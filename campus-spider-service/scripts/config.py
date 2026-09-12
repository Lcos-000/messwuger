# -*- coding: utf-8 -*-
"""
爬虫脚本公共配置，统一从环境变量读取。
敏感配置必须通过环境变量注入。
"""

import os

# 云打码平台配置
YM_TOKEN = os.getenv("YM_TOKEN", "")
YM_TYPE = os.getenv("YM_TYPE", "10110")
