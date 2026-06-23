import requests

def get_email(token):
    url = "https://of.swu.edu.cn/gateway/fighter-middle/api/auth/user?appType=fighter-portal"
    headers = {"fighter-auth-token": token}
    email = requests.get(url, headers=headers).json()["data"]["subject"]["email"]
    return email

def get_student_id(token):
    url = "https://of.swu.edu.cn/gateway/fighter-middle/api/auth/user?appType=fighter-portal"
    headers = {"fighter-auth-token": token}
    student_id = requests.get(url, headers=headers).json()["data"]["subject"]["username"]
    return student_id
