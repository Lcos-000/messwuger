# -*- coding: utf-8 -*-
"""
爬虫脚本公共配置，统一从环境变量读取，保留默认值便于本地开发。
生产环境建议通过环境变量覆盖敏感配置。
"""

import os

# 云打码平台配置
YM_TOKEN = os.getenv("YM_TOKEN", "BVGx1jNKFdim4QalbgIR9m-mcwfxe_fS3Ro14yAPZrM")
YM_TYPE = os.getenv("YM_TYPE", "10110")
