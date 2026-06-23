import base64
import json
import os
import re
import subprocess
import urllib.parse
from urllib.parse import urlparse

import requests
import urllib3

urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

YM_TOKEN = os.getenv("YM_TOKEN", "BVGx1jNKFdim4QalbgIR9m-mcwfxe_fS3Ro14yAPZrM")
YM_TYPE = os.getenv("YM_TYPE", "10110")

IDM_BASE = "https://idm.swu.edu.cn"


def _des_encrypt(data: str, key: str) -> str:
    """使用 Node.js + des.js 进行 DES 加密"""
    des_js_path = os.path.join(os.path.dirname(__file__), "des.js")
    if not os.path.exists(des_js_path):
        raise RuntimeError("des.js 不存在")

    js_path_js = des_js_path.replace("\\", "/").replace("'", "\\'")
    safe_data = data.replace("'", "\\'")
    safe_key = key.replace("'", "\\'")
    js_code = f"""
var fs = require('fs');
var code = fs.readFileSync('{js_path_js}', 'utf8');
eval(code);
console.log(strEnc('{safe_data}', '{safe_key}', "", ""));
"""
    result = subprocess.run(
        ["node", "-e", js_code],
        capture_output=True, text=True, timeout=15
    )
    if result.returncode != 0:
        raise RuntimeError(f"Node.js DES 加密失败: {result.stderr}")
    return result.stdout.strip()


def _solve_captcha(captcha_bytes: bytes) -> str:
    """识别验证码：优先云打码，失败则抛出异常"""
    if not YM_TOKEN:
        raise RuntimeError("未配置 YM_TOKEN，无法自动识别验证码")

    try:
        b64 = base64.b64encode(captcha_bytes).decode()
        url = "http://api.jfbym.com/api/YmServer/customApi"
        payload = {
            "token": YM_TOKEN,
            "type": YM_TYPE,
            "image": b64,
        }
        resp = requests.post(
            url,
            headers={"Content-Type": "application/json"},
            json=payload,
            timeout=20,
        ).json()

        inner = resp.get("data", {}) or {}
        code = inner.get("data", "").strip() if isinstance(inner.get("data"), str) else ""
        if code:
            return code
    except Exception as e:
        print(f"[captcha] 云打码失败: {e}")

    raise RuntimeError("验证码识别失败")


def transform(ticket):
    """钉钉打卡 callback 所需的 ticket 变换"""
    ticket = urllib.parse.unquote(ticket).split("-")
    str1 = ""
    str2 = ""
    for i in ticket[1]:
        str1 += str((int(i) + 5) % 10)
    for i in ticket[2]:
        if "0" <= i <= "9":
            str2 += str((int(i) + 5) % 10)
        elif 'A' <= i <= 'Z':
            if ord(i) + 10 > ord('Z'):
                str2 += chr(ord(i) + 10 - 26)
            else:
                str2 += chr(ord(i) + 10)
        else:
            if ord(i) + 15 > ord('z'):
                str2 += chr(ord(i) + 15 - 26)
            else:
                str2 += chr(ord(i) + 15)
    return str1, str2


def get_token(username: str, password: str) -> str:
    session = requests.Session()
    session.trust_env = False
    session.headers.update({
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:151.0) Gecko/20100101 Firefox/151.0",
        "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
        "Accept-Language": "zh-CN,zh;q=0.9",
    })

    # Step 1: 获取 state
    oauth_url = (
        "https://of.swu.edu.cn/cas/oauth/login/SWU_CAS2_FEDERAL"
        "?service=https%3A%2F%2Fof.swu.edu.cn%2Fgateway%2Ffighter-middle%2Fapi%2Fintegrate%2Fuaap%2Fcas%2Fresolve-cas-return"
        "%3Fnext%3Dhttps%253A%252F%252Fof.swu.edu.cn%252F%2523%252FcasLogin%253Ffrom%253D%25252FappCenter"
        "&federalEnable=true"
    )
    r = session.get(oauth_url, timeout=15, verify=False, allow_redirects=True)

    parsed = urllib.parse.urlparse(r.url)
    qs = urllib.parse.parse_qs(parsed.query)

    state = ""
    if "state" in qs:
        state = qs["state"][0]
    elif "originalRequestUrl" in qs:
        orig_url = urllib.parse.unquote(qs["originalRequestUrl"][0])
        orig_parsed = urllib.parse.urlparse(orig_url)
        orig_qs = urllib.parse.parse_qs(orig_parsed.query)
        if "state" in orig_qs:
            state = orig_qs["state"][0]

    if not state:
        raise RuntimeError("无法获取 state 参数")

    # Step 2: CAS 登录入口 -> IDM 登录页
    cas_entry = (
        "https://uaaap.swu.edu.cn/cas/login"
        "?service=https%3A%2F%2Fof.swu.edu.cn%2Fgateway%2Ffighter-middle%2Fapi%2Fintegrate%2Fuaap%2Fcas%2Fresolve-cas-return"
        "%3Fnext%3Dhttps%253A%252F%252Fof.swu.edu.cn%252F%2523%252FcasLogin%253Ffrom%253D%25252FappCenter"
        "&federalEnable=true"
    )

    r = session.get(cas_entry, timeout=15, verify=False, allow_redirects=True)
    if "idm.swu.edu.cn/am/UI/Login" not in r.url:
        raise RuntimeError("未能到达 IDM 登录页")

    text = r.text
    m = re.search(r'id="random"[^>]*value="([^"]+)"', text)
    random_val = m.group(1) if m else ""
    if not random_val:
        raise RuntimeError("未能获取 random 加密密钥")

    # Step 3: 下载并识别验证码
    captcha_url = f"{IDM_BASE}/am/validate.code?id=0.123456"
    rc = session.get(captcha_url, timeout=15, verify=False)
    captcha = _solve_captcha(rc.content)

    # Step 4: DES 加密用户名密码
    enc_user = _des_encrypt(username, random_val)
    enc_pass = _des_encrypt(password, random_val)

    login_data = {
        "IDToken1": enc_user,
        "IDToken2": enc_pass,
        "IDToken3": "",
        "goto": "",
        "gotoOnFail": "",
        "SunQueryParamsString": "cmVhbG09LyZzZXJ2aWNlPWluaXRTZXJ2aWNlJg==",
        "encoded": "true",
        "validateCode": captcha,
        "gx_charset": "UTF-8",
    }

    gm = re.search(r'name="goto"[^>]*value="([^"]*)"', text)
    if gm and gm.group(1):
        login_data["goto"] = gm.group(1)

    gm2 = re.search(r'name="SunQueryParamsString"[^>]*value="([^"]*)"', text)
    if gm2:
        login_data["SunQueryParamsString"] = gm2.group(1)

    # Step 5: 提交登录
    r2 = session.post(
        f"{IDM_BASE}/am/UI/Login",
        data=login_data, timeout=15, verify=False, allow_redirects=True
    )

    parsed2 = urllib.parse.urlparse(r2.url)
    qs2 = urllib.parse.parse_qs(parsed2.query)
    if "ticket" not in qs2:
        if "密码错误" in r2.text or "不正确" in r2.text:
            raise RuntimeError("用户名或密码错误")
        raise RuntimeError("登录后未获取到 ticket")
    ticket = qs2["ticket"][0]

    # Step 6: callback 获取 ST
    str1, str2 = transform(ticket)
    CD = f"CD-{str1}-{str2}-wiie://777.643.675.751:3537/rph"
    callback_url = (
        f"https://of.swu.edu.cn/cas/oauth/callback/SWU_CAS2_FEDERAL"
        f"?code={CD}@@hxbeat&state={state}"
    )

    r3 = session.get(callback_url, timeout=15, verify=False, allow_redirects=True)

    # ST 可能在 URL fragment 中 (#/casLogin?from=...&ticket=...)
    parsed3 = urllib.parse.urlparse(r3.url)
    qs3 = urllib.parse.parse_qs(parsed3.query)
    ST = ""
    if "ticket" in qs3:
        ST = qs3["ticket"][0]
    elif parsed3.fragment:
        frag_parsed = urllib.parse.urlparse("?" + parsed3.fragment)
        frag_qs = urllib.parse.parse_qs(frag_parsed.query)
        if "ticket" in frag_qs:
            ST = frag_qs["ticket"][0]

    if not ST:
        raise RuntimeError("callback 后未获取到 ST")

    # Step 7: exchange ST for fighter-auth-token
    token_resp = session.get(
        f"https://of.swu.edu.cn/gateway/fighter-middle/api/integrate/uaap/cas/exchange-token"
        f"?token={ST}&remember=true",
        timeout=15, verify=False
    ).json()

    if token_resp.get("code") != 200 or not token_resp.get("data"):
        raise RuntimeError(f"登录失败：无法获取访问令牌: {token_resp.get('msg', token_resp)}")

    return token_resp["data"]
