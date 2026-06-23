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

    # 4. 构造打卡提交请求
    url = "https://of.swu.edu.cn/gateway/fighter-baida/api/form-instance/save"
    params = {"formId": formid, "isSubmitProcess": False}
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

    # 添加位置信息：完全依赖系统返回的地址
    if "qddz" not in payload or not payload["qddz"]:
        raise Exception("系统未返回地址信息，无法完成打卡")
    
    location = payload["qddz"]
    if isinstance(location, str):
        try:
            location = json.loads(location)
        except Exception as e:
            raise Exception(f"解析地址信息失败: {e}")
    
    location["time"] = int(time.time() * 1000)
    payload["qddz"] = location

    # 5. 提交打卡
    resp = requests.post(url, headers=headers, params=params, json=payload, timeout=30)
    result = resp.json()

    if result.get("code") != 200:
        raise Exception(f"打卡提交失败: {result.get('msg', result)}")

    # 6. 位置验证 (cqlc/verify)
    verify_url = "https://of.swu.edu.cn/gateway/fighter-baida/api/cqlc/verify"
    verify_params = {"businessKey": business_key}
    verify_payload = {"mapData": location}
    verify_resp = requests.post(verify_url, headers=headers, params=verify_params, json=verify_payload, timeout=30)
    verify_result = verify_resp.json()

    if verify_result.get("code") != 200:
        raise Exception(f"位置验证失败: {verify_result.get('msg', verify_result)}")

    return result.get("data")
