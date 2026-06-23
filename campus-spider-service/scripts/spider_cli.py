#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import argparse
import json
import os
import sys

from swu_kb import SWUJwClient


# 主函数，处理命令行参数并调用 SWUJwClient 类
def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--mode", choices=["crawl", "validate"], default="crawl")
    parser.add_argument("--student-id", required=True)
    parser.add_argument("--password", required=True)
    parser.add_argument("--xnm", default=os.getenv("XNM", "2025"))
    parser.add_argument("--xqm", default=os.getenv("XQM", "12"))
    parser.add_argument("--session-dir", default="./data/sessions")
    parser.add_argument("--proxy", default="")
    args = parser.parse_args()

    proxies = None
    if args.proxy.strip():
        proxies = {
            "http": args.proxy.strip(),
            "https": args.proxy.strip(),
        }

    session_file = os.path.join(args.session_dir, f"session_{args.student_id}.json")
    client = SWUJwClient(
        session_file=session_file,
        session_dir=args.session_dir,
        allow_manual_captcha=False,
        proxies=proxies,
    )

    try:
        ok = client.login(args.student_id, args.password)
        if not ok:
            print(json.dumps({
                "success": False,
                "message": "登录失败"
            }, ensure_ascii=False))
            return 1

        if args.mode == "validate":
            print(json.dumps({
                "success": True,
                "message": "credentials valid",
                "data": {
                    "studentId": args.student_id
                }
            }, ensure_ascii=False))
            return 0

        kb_data = client.get_kb(args.xnm, args.xqm)
        data = client.build_result(args.student_id, args.xnm, args.xqm, kb_data)

        print(json.dumps({
            "success": True,
            "message": "crawl success",
            "data": data
        }, ensure_ascii=False))
        return 0

    except Exception as e:
        print(json.dumps({
            "success": False,
            "message": str(e)
        }, ensure_ascii=False))
        return 1


# 主函数入口
if __name__ == "__main__":
    sys.exit(main())
