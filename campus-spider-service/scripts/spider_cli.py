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
    parser.add_argument("--mode",
                        choices=["crawl", "validate", "empty-classroom", "grades"],
                        default="crawl")
    parser.add_argument("--student-id", required=True)
    parser.add_argument("--password", required=True)
    parser.add_argument("--xnm", default=os.getenv("XNM", "2025"))
    parser.add_argument("--xqm", default=os.getenv("XQM", "12"))
    parser.add_argument("--session-dir", default="./data/sessions")
    parser.add_argument("--proxy", default="")

    # 空教室专用参数
    parser.add_argument("--xqj", default="1", help="星期几（1=周一，7=周日）")
    parser.add_argument("--jcd", default="", help="节次掩码整数，例如 16")
    parser.add_argument("--jcd-text", default="", help="节次文本，例如 '5-5' 或 '1-2,5-6'")
    parser.add_argument("--zcd", default="", help="周次掩码整数，例如 262272")
    parser.add_argument("--zcd-text", default="", help="周次文本，例如 '1-16'")
    parser.add_argument("--xqh-id", default="1", help="校区号，默认 1")
    parser.add_argument("--lh", default="", help="楼号，例如 32")
    parser.add_argument("--cdlb-id", default="", help="教室类别 ID")

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

        if args.mode == "empty-classroom":
            jcd = args.jcd
            if not jcd and args.jcd_text:
                jcd = str(SWUJwClient.encode_periods(args.jcd_text))
            if not jcd:
                print(json.dumps({
                    "success": False,
                    "message": "空教室查询需要 --jcd 或 --jcd-text"
                }, ensure_ascii=False))
                return 1

            zcd = args.zcd
            if not zcd and args.zcd_text:
                zcd = str(SWUJwClient.encode_weeks(args.zcd_text))
            if not zcd:
                print(json.dumps({
                    "success": False,
                    "message": "空教室查询需要 --zcd 或 --zcd-text"
                }, ensure_ascii=False))
                return 1

            raw = client.get_empty_classrooms(
                args.xnm, args.xqm, args.xqj, jcd, zcd,
                xqh_id=args.xqh_id, lh=args.lh, cdlb_id=args.cdlb_id
            )
            data = client.build_empty_classroom_result(
                args.student_id, args.xnm, args.xqm, args.xqj, jcd, zcd, raw
            )
            print(json.dumps({
                "success": True,
                "message": "empty classroom query success",
                "data": data
            }, ensure_ascii=False))
            return 0

        if args.mode == "grades":
            raw = client.get_grades(args.xnm, args.xqm)
            data = client.build_grades_result(args.student_id, args.xnm, args.xqm, raw)
            print(json.dumps({
                "success": True,
                "message": "grades query success",
                "data": data
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
