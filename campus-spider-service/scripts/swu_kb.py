#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import base64
import json
import os
import re
import subprocess
import sys
from urllib.parse import urlparse

import requests
import urllib3

urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

YM_TOKEN = os.getenv("YM_TOKEN", "BVGx1jNKFdim4QalbgIR9m-mcwfxe_fS3Ro14yAPZrM")
YM_TYPE = os.getenv("YM_TYPE", "10110")


def eprint(*args, **kwargs):
    print(*args, file=sys.stderr, **kwargs)


# SWUJwClient 类，用于与 SWU 务务系统交互
class SWUJwClient:
    def __init__(self, session_file=None, session_dir="./data/sessions", allow_manual_captcha=False, proxies=None):
        self.session = requests.Session()
        self.session.trust_env = False
        self.session.headers.update({
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:151.0) Gecko/20100101 Firefox/151.0",
            "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
            "Accept-Language": "zh-CN,zh;q=0.9",
        })
        if proxies:
            self.session.proxies.update(proxies)

        self.jw_base = "https://jw.swu.edu.cn/jwglxt"
        self.idm_base = "https://idm.swu.edu.cn"

        self.session_dir = session_dir
        os.makedirs(self.session_dir, exist_ok=True)

        self.session_file = session_file
        self.allow_manual_captcha = allow_manual_captcha

    def _des_encrypt(self, data: str, key: str) -> str:
        des_js_path = os.path.join(os.path.dirname(__file__), "des.js")
        if not os.path.exists(des_js_path):
            raise RuntimeError("des.js 不存在，请确认已放入 scripts 目录")

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

    def solve_captcha(self, captcha_bytes: bytes) -> str:
        if not YM_TOKEN:
            if not self.allow_manual_captcha:
                raise RuntimeError("未配置 YM_TOKEN，且不允许手工验证码")
            captcha = input("请输入验证码: ").strip()
            return captcha

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
            eprint(f"[captcha] 云打码失败: {e}")

        if not self.allow_manual_captcha:
            raise RuntimeError("验证码识别失败")
        return input("验证码识别失败，请手动输入: ").strip()

    def _follow_one_redirect(self, response, current_url: str) -> str:
        location = response.headers.get("Location", "")
        if location.startswith("http"):
            return location
        elif location.startswith("/"):
            parsed = urlparse(current_url)
            return f"{parsed.scheme}://{parsed.netloc}{location}"
        elif location:
            return current_url.rsplit("/", 1)[0] + "/" + location
        return current_url

    def _extract_js_redirect(self, html: str) -> str:
        m = re.search(r'window\.location(?:\.href|\.replace)?\s*=\s*[\'"]([^\'"]+)[\'"]', html)
        if m:
            return m.group(1)
        m = re.search(r'(?:self|top)\.location(?:\.href)?\s*=\s*[\'"]([^\'"]+)[\'"]', html)
        if m:
            return m.group(1)
        m = re.search(r'<meta[^>]+http-equiv=[\'"]refresh[\'"][^>]+url=([^\'";\s]+)', html, re.I)
        if m:
            return m.group(1)
        return ""

    def _resolve_url(self, url: str, base_url: str) -> str:
        if url.startswith("http"):
            return url
        parsed = urlparse(base_url)
        if url.startswith("/"):
            return f"{parsed.scheme}://{parsed.netloc}{url}"
        return base_url.rsplit("/", 1)[0] + "/" + url

    def _is_logged_into_jw(self, url: str) -> bool:
        return "jw.swu.edu.cn" in url and "login_slogin" not in url and "sso/zllogin" not in url

    def _reset_session(self):
        self.session = requests.Session()
        self.session.trust_env = False
        self.session.headers.update({
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:151.0) Gecko/20100101 Firefox/151.0",
            "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
            "Accept-Language": "zh-CN,zh;q=0.9",
        })

    def login(self, username: str, password: str) -> bool:
        session_file = self.session_file or os.path.join(self.session_dir, f"session_{username}.json")
        self.session_file = session_file

        if self.load_session() and self.is_session_valid():
            return True

        # Session 已过期，重建 Session 避免旧 cookies 污染 CAS 登录流程
        self._reset_session()

        cas_entry = (
            "https://uaaap.swu.edu.cn/cas/login"
            "?service=https%3A%2F%2Fjw.swu.edu.cn%2Fsso%2Fzllogin"
            "&federalEnable=true"
        )

        current_url = cas_entry
        for _ in range(5):
            r = self.session.get(current_url, timeout=15, verify=False, allow_redirects=False)
            if r.status_code in (301, 302, 303, 307, 308):
                current_url = self._follow_one_redirect(r, current_url)
                continue
            break

        if "idm.swu.edu.cn/am/UI/Login" not in current_url:
            raise RuntimeError("未能到达 IDM 登录页")

        r = self.session.get(current_url, timeout=15, verify=False)
        text = r.text

        m = re.search(r'id="random"[^>]*value="([^"]+)"', text)
        random_val = m.group(1) if m else ""
        if not random_val:
            raise RuntimeError("未能获取 random 加密密钥")

        captcha_url = f"{self.idm_base}/am/validate.code?id=0.123456"
        rc = self.session.get(captcha_url, timeout=15, verify=False)
        captcha = self.solve_captcha(rc.content)

        enc_user = self._des_encrypt(username, random_val)
        enc_pass = self._des_encrypt(password, random_val)

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

        r2 = self.session.post(
            f"{self.idm_base}/am/UI/Login",
            data=login_data, timeout=15, verify=False, allow_redirects=False
        )

        current_url = r2.headers.get("Location", "") if r2.status_code in (301, 302) else ""
        if not current_url:
            if "密码错误" in r2.text or "不正确" in r2.text:
                raise RuntimeError("用户名或密码错误")
            raise RuntimeError("登录失败，无重定向")

        for _ in range(20):
            if not current_url.startswith("http"):
                break

            r = self.session.get(current_url, timeout=15, verify=False, allow_redirects=False)

            if r.status_code in (301, 302, 303, 307, 308):
                current_url = self._follow_one_redirect(r, current_url)
                continue

            if self._is_logged_into_jw(current_url):
                self.save_session()
                return True

            js_redirect = self._extract_js_redirect(r.text)
            if js_redirect:
                current_url = self._resolve_url(js_redirect, current_url)
                continue

            if "idm.swu.edu.cn" in current_url and "index.jsp" in current_url:
                current_url = (
                    f"{self.idm_base}/am/oauth2/authorize"
                    f"?service=initService&response_type=code&client_id=7c1zokoljl9bbiho6yuo"
                    f"&scope=uid+cn+userIdCode"
                    f"&redirect_uri=https%3A%2F%2Fuaaap.swu.edu.cn%2Fcas%2Flogin"
                    f"%3Fservice%3Dhttps%253A%252F%252Fuaaap.swu.edu.cn%252Fcas%252Foauth2.0%252FcallbackAuthorize"
                    f"&decision=Allow"
                )
                continue

            if "uaaap.swu.edu.cn/cas/login" in current_url:
                current_url = "https://uaaap.swu.edu.cn/cas/login?service=https%3A%2F%2Fjw.swu.edu.cn%2Fsso%2Fzllogin"
                continue

            break

        if self._is_logged_into_jw(current_url):
            self.save_session()
            return True

        raise RuntimeError("登录流程未完成")

    def get_kb(self, xnm: str, xqm: str) -> dict:
        index_url = f"{self.jw_base}/kbcx/xskbcx_cxXskbcxIndex.html?gnmkdm=N253508&layout=default"
        self.session.get(index_url, timeout=15, verify=False)

        api_url = f"{self.jw_base}/kbcx/xskbcx_cxXsgrkb.html?gnmkdm=N253508"
        headers = {
            "X-Requested-With": "XMLHttpRequest",
            "Content-Type": "application/x-www-form-urlencoded;charset=utf-8",
            "Referer": index_url,
        }
        data = {
            "xnm": xnm,
            "xqm": xqm,
            "kzlx": "ck",
            "xsdm": "",
            "kclbdm": "",
            "kclxdm": "",
        }
        r = self.session.post(api_url, data=data, headers=headers, timeout=15, verify=False)
        if r.status_code != 200:
            raise RuntimeError(f"课表API请求失败: {r.status_code}, body={r.text[:500]}")
        kb_data = r.json()
        if not isinstance(kb_data, dict):
            raise RuntimeError(f"课表API返回非JSON对象: type={type(kb_data).__name__}, body={r.text[:500]}")
        return kb_data

    def build_result(self, student_id: str, xnm: str, xqm: str, kb_data: dict) -> dict:
        xsxx = kb_data.get("xsxx", {}) or {}

        def pick(d, keys, default=""):
            for k in keys:
                v = d.get(k)
                if v not in (None, ""):
                    return str(v)
            return default

        personal = {
            "studentId": student_id or pick(xsxx, ["XH", "xh"]),
            "name": pick(xsxx, ["XM", "xm"]),
            "major": pick(xsxx, ["ZYMC", "zymc", "major"]),
            "className": pick(xsxx, ["BJMC", "bjmc", "className"]),
            "college": pick(xsxx, ["YXMC", "yxmc", "college"]),
        }

        schedule = []
        for item in kb_data.get("kbList", []) or []:
            schedule.append({
                "courseName": item.get("kcmc", ""),
                "teacher": item.get("xm", ""),
                "campus": item.get("xqmc", "") or item.get("cdmc", ""),
                "classroom": item.get("cdmc", ""),
                "dayOfWeek": int(item.get("xqj", "0") or 0),
                "periods": item.get("jc", "") or item.get("jcs", ""),
                "weeks": item.get("zcd", ""),
                "courseType": "regular",
            })

        for item in kb_data.get("sjkList", []) or []:
            schedule.append({
                "courseName": item.get("kcmc", ""),
                "teacher": item.get("jsxm", ""),
                "campus": "",
                "classroom": item.get("qsjsz", ""),
                "dayOfWeek": 0,
                "periods": "",
                "weeks": "",
                "courseType": "practice",
            })

        return {
            "studentId": personal["studentId"],
            "academicYear": xnm,
            "semester": xqm,
            "personalInfo": personal,
            "scheduleData": schedule,
        }

    def save_session(self):
        if not self.session_file:
            return
        cookies = {}
        for domain_cookie in self.session.cookies._cookies.values():
            for path_cookie in domain_cookie.values():
                for name, cookie in path_cookie.items():
                    cookies[f"{name}@{cookie.domain}"] = cookie.value
        with open(self.session_file, "w", encoding="utf-8") as f:
            json.dump(cookies, f, ensure_ascii=False, indent=2)

    def load_session(self):
        if not self.session_file or not os.path.exists(self.session_file):
            return False
        with open(self.session_file, "r", encoding="utf-8") as f:
            cookies = json.load(f)
        for key, value in cookies.items():
            if "@" in key:
                name, domain = key.split("@", 1)
                self.session.cookies.set(name, value, domain=domain)
        return True

    def is_session_valid(self) -> bool:
        check = self.session.get(
            "https://jw.swu.edu.cn/jwglxt/xtgl/index_initMenu.html",
            timeout=15, verify=False, allow_redirects=False
        )
        return check.status_code == 200 and "login_slogin" not in check.url
