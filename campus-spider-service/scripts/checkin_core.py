import requests
import time
import json
from checkin_transition import get_transition_today
from checkin_info import get_student_id

def get_form_instance(token, data_id, form_id):
    """获取表单完整数据，用于补全提交字段"""
    url = "https://of.swu.edu.cn/gateway/fighter-baida/api/form-instance/select"
    params = {"dataId": data_id, "formId": form_id, "procDefId": ""}
    headers = {"fighter-auth-token": token}
    resp = requests.get(url, headers=headers, params=params, timeout=30)
    result = resp.json()
    if result.get("code") != 200:
        raise Exception(f"获取表单数据失败: {result.get('msg', result)}")
    return result.get("data", {})


def get_dormitory(token, payload):
    """根据宿舍/签到范围获取系统允许的打卡地址和经纬度"""
    url = "https://of.swu.edu.cn/gateway/fighter-baida/api/cqlc/getDormitory"
    headers = {
        "fighter-auth-token": token,
        "Content-Type": "application/json;charset=UTF-8"
    }
    resp = requests.post(url, headers=headers, json=payload, timeout=30)
    result = resp.json()
    if result.get("code") != 200:
        raise Exception(f"获取宿舍地址失败: {result.get('msg', result)}")

    columns = result.get("data", {}).get("columnList", [])
    if not columns:
        raise Exception("宿舍地址返回为空")

    # 第一个元素包含 address / latitude / longitude / qdbj
    return columns[0]


def build_map_data(address, latitude, longitude):
    """构造位置验证需要的 mapData（字段从抓包中提取）"""
    now_ms = int(time.time() * 1000)
    return {
        "errorCode": 0,
        "errorMessage": "",
        "locationType": 6,
        "accuracy": 550,
        "latitude": float(latitude),
        "longitude": float(longitude),
        "province": "重庆市",
        "city": "重庆市",
        "district": "北碚区",
        "road": "天生路",
        "address": address,
        "netType": "5g",
        "operatorType": "unknown",
        "imei": "imei",
        "time": now_ms,
        "provider": "lbs",
        "isFromMock": False,
        "isGpsEnabled": True,
        "isWifiEnabled": False,
        "isMobileEnabled": True,
        "isOffset": True,
        "cityAdCode": "023",
        "districtAdCode": "500109",
        "LBSWuaCacheId": ""
    }


def checkin(token):
    # 1. 获取今日任务
    transition = get_transition_today(token)
    if transition is None:
        return None

    formid = transition.get("formId")
    data_id = transition.get("id")
    business_key = data_id

    # 2. 获取学号
    try:
        xh = get_student_id(token)
    except Exception as e:
        print(f"获取学号失败: {e}")
        xh = ""

    # 3. 获取表单完整数据（补全字段）
    try:
        form_data = get_form_instance(token, data_id, formid)
    except Exception as e:
        print(f"获取表单数据失败: {e}")
        form_data = {}

    now = time.strftime("%Y-%m-%d %H:%M")
    today = time.strftime("%Y-%m-%d")

    headers = {
        "fighter-auth-token": token,
        "Content-Type": "application/json;charset=UTF-8"
    }

    payload = {
        "id": data_id,
        "formId": formid,
        "tsrq": today,
        "xh": xh,
        "qdsj": ["21:00", "23:30"],
        "dksj": now,
    }

    # 从 transition / form_data 中提取其他关键字段
    for key in ["qdjg", "$qdjg", "cqfbid", "qdtj", "ycdksfcl", "isArchive", "qsqddd", "qdbj", "qddz"]:
        if key in transition and transition[key] is not None:
            payload[key] = transition[key]
        elif key in form_data and form_data[key] is not None:
            payload[key] = form_data[key]

    # 设置默认值（根据抓包数据）
    if "qdjg" not in payload:
        payload["qdjg"] = "0"
    if "$qdjg" not in payload:
        payload["$qdjg"] = "未签到"
    if "qdtj" not in payload:
        payload["qdtj"] = "2"
    if "ycdksfcl" not in payload:
        payload["ycdksfcl"] = ""
    if "isArchive" not in payload:
        payload["isArchive"] = ""

    # 4. 从宿舍/签到范围接口获取系统允许的打卡地址
    dormitory = get_dormitory(token, payload)
    address = dormitory.get("address", "")
    latitude = dormitory.get("latitude", "")
    longitude = dormitory.get("longitude", "")
    qdbj = dormitory.get("qdbj", 800)
    if not address or not latitude or not longitude:
        raise Exception("系统未返回有效的宿舍地址信息")

    # 后端要求 qddz 为 Map，qsqddd/qdbj 为展示字符串
    map_data = build_map_data(address, latitude, longitude)
    payload["qddz"] = map_data
    payload["qsqddd"] = address
    payload["qdbj"] = f"{qdbj}米" if isinstance(qdbj, int) else qdbj

    # 5. 位置验证 (cqlc/verify)
    verify_url = "https://of.swu.edu.cn/gateway/fighter-baida/api/cqlc/verify"
    verify_params = {"businessKey": business_key}
    verify_payload = {"mapData": map_data}
    verify_resp = requests.post(verify_url, headers=headers, params=verify_params, json=verify_payload, timeout=30)
    verify_result = verify_resp.json()

    if verify_result.get("code") != 200:
        raise Exception(f"位置验证失败: {verify_result.get('msg', verify_result)}")
    if not verify_result.get("data", {}).get("isArea", False):
        raise Exception(f"当前不在签到范围内: {verify_result.get('data', {}).get('tip', '')}")

    # 6. 提交打卡
    save_url = "https://of.swu.edu.cn/gateway/fighter-baida/api/form-instance/save"
    save_params = {"formId": formid, "isSubmitProcess": False}
    resp = requests.post(save_url, headers=headers, params=save_params, json=payload, timeout=30)
    result = resp.json()

    if result.get("code") != 200:
        raise Exception(f"打卡提交失败: {result.get('msg', result)}")

    return result.get("data")
