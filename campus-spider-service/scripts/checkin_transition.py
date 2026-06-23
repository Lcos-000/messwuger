import requests

def get_transition_today(token):
    url = "https://of.swu.edu.cn/gateway/fighter-baida/api/cqtj/getTransitionByToday"
    headers = {"fighter-auth-token": token}
    data = {"pageNum": 1, "pageSize": 5}
    resp = requests.post(url, headers=headers, data=data, timeout=30)
    result = resp.json()
    if result.get("code") != 200:
        raise Exception(f"获取今日任务失败: {result.get('msg', result)}")
    records = result.get("data", {}).get("records", [])
    return records[0] if records else None
