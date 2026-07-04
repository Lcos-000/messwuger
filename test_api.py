import base64
import json
import requests
from Crypto.Cipher import AES

KEY = "@aes-secret-key#".encode("utf-8")
IV = KEY[:16]
STUDENT_ID = "222025321262104"
PASSWORD = "13404032505@lyx"
GO_BASE = "http://localhost:8082"


def aes_encrypt(plain_text: str) -> str:
    cipher = AES.new(KEY, AES.MODE_CBC, IV)
    data = plain_text.encode("utf-8")
    pad_len = 16 - (len(data) % 16)
    data += bytes([pad_len]) * pad_len
    return base64.b64encode(cipher.encrypt(data)).decode("utf-8")


def call_empty_classroom():
    headers = {
        "X-Student-Id": STUDENT_ID,
        "X-Password": aes_encrypt(PASSWORD),
        "Content-Type": "application/json",
    }
    body = {
        "academicYear": "2025",
        "semester": "12",
        "dayOfWeek": "3,6",
        "periodsMask": "3072",
        "weeksMask": "65535",
        "campusId": "2",
        "building": "01",
        "roomType": "",
    }
    r = requests.post(f"{GO_BASE}/api/v1/task/empty-classroom", headers=headers, json=body, timeout=10)
    print("empty-classroom submit:", r.status_code, r.text)


def call_grades():
    headers = {
        "X-Student-Id": STUDENT_ID,
        "X-Password": aes_encrypt(PASSWORD),
        "Content-Type": "application/json",
    }
    body = {
        "academicYear": "2025",
        "semester": "12",
    }
    r = requests.post(f"{GO_BASE}/api/v1/task/grades", headers=headers, json=body, timeout=10)
    print("grades submit:", r.status_code, r.text)


if __name__ == "__main__":
    call_empty_classroom()
    call_grades()
