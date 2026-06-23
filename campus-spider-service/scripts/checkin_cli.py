#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import argparse
import json
import sys

from checkin_token import get_token
from checkin_transition import get_transition_today
from checkin_core import checkin


def main():
    parser = argparse.ArgumentParser(description="SWU daily check-in runner")
    parser.add_argument("--student-id", required=True, help="SWU student ID")
    parser.add_argument("--password", required=True, help="SWU password (plaintext)")
    parser.add_argument("--force", action="store_true", help="强制触发打卡，忽略已签到状态")
    args = parser.parse_args()

    try:
        token = get_token(args.student_id, args.password)
        if not token:
            print(json.dumps({
                "success": False,
                "message": "登录失败，无法获取token",
                "data": {"studentId": args.student_id, "status": "login_failed"}
            }, ensure_ascii=False))
            return 1

        transition = get_transition_today(token)
        if not transition:
            print(json.dumps({
                "success": True,
                "message": "今日暂无打卡任务",
                "data": {"studentId": args.student_id, "status": "no_task"}
            }, ensure_ascii=False))
            return 0

        if transition.get("qdzt") == "已签到" and not args.force:
            print(json.dumps({
                "success": True,
                "message": "今日已打卡",
                "data": {"studentId": args.student_id, "status": "already_checked"}
            }, ensure_ascii=False))
            return 0

        result = checkin(token)
        if result is not None:
            print(json.dumps({
                "success": True,
                "message": "打卡成功",
                "data": {"studentId": args.student_id, "status": "success"}
            }, ensure_ascii=False))
            return 0
        else:
            print(json.dumps({
                "success": False,
                "message": "打卡请求失败",
                "data": {"studentId": args.student_id, "status": "failed"}
            }, ensure_ascii=False))
            return 1

    except Exception as e:
        print(json.dumps({
            "success": False,
            "message": str(e),
            "data": {"studentId": args.student_id, "status": "error"}
        }, ensure_ascii=False))
        return 1


if __name__ == "__main__":
    sys.exit(main())
