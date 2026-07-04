import base64
import xml.etree.ElementTree as ET
import urllib.parse
import json

tree = ET.parse(r'c:\Users\罗宇轩\Desktop\campus(1)(4)\12345.md')
root = tree.getroot()

print("=" * 60)
print("1. 所有 cdjy_cxKxcdlb.html 空教室查询请求的参数")
print("=" * 60)

for idx, item in enumerate(root.findall('item')):
    url = item.find('url').text or ''
    if 'cdjy_cxKxcdlb.html' not in url:
        continue

    req_b64 = item.find('request').text or ''
    req_bytes = base64.b64decode(req_b64)

    header_end = req_bytes.find(b'\r\n\r\n')
    sep = 4
    if header_end == -1:
        header_end = req_bytes.find(b'\n\n')
        sep = 2

    body = req_bytes[header_end + sep:].decode('utf-8', errors='replace')

    print(f'\n--- 请求 #{idx + 1}: {url} ---')
    if body:
        parsed = urllib.parse.parse_qs(body)
        for k, v in sorted(parsed.items()):
            print(f'  {k} = {v[0] if len(v) == 1 else v}')
    else:
        print('  (GET 请求，无 body)')

print("\n" + "=" * 60)
print("2. 空教室查询响应中的字段示例（第一条记录）")
print("=" * 60)

for idx, item in enumerate(root.findall('item')):
    url = item.find('url').text or ''
    if 'cdjy_cxKxcdlb.html' not in url:
        continue

    resp_b64 = item.find('response').text or ''
    resp_bytes = base64.b64decode(resp_b64)
    # 跳过 HTTP 头
    header_end = resp_bytes.find(b'\r\n\r\n')
    sep = 4
    if header_end == -1:
        header_end = resp_bytes.find(b'\n\n')
        sep = 2
    body = resp_bytes[header_end + sep:].decode('utf-8', errors='replace')

    try:
        data = json.loads(body)
        items = data.get('items', [])
        if items:
            print('\n第一条记录字段：')
            for k, v in sorted(items[0].items()):
                print(f'  {k} = {v}')
        print(f'\n总记录数: {len(items)}')
    except Exception as e:
        print(f'解析响应失败: {e}')
    break

print("\n" + "=" * 60)
print("3. 查找可能包含楼号/校区/教室类别的其他请求")
print("=" * 60)

keywords = ['cdlb', 'xqh', 'lh', 'campus', 'building', 'cdxq']
seen_urls = set()
for item in root.findall('item'):
    url = item.find('url').text or ''
    lower_url = url.lower()
    if any(k in lower_url for k in keywords) and url not in seen_urls:
        seen_urls.add(url)
        print(f'\nURL: {url}')

        req_b64 = item.find('request').text or ''
        req_bytes = base64.b64decode(req_b64)
        header_end = req_bytes.find(b'\r\n\r\n')
        sep = 4
        if header_end == -1:
            header_end = req_bytes.find(b'\n\n')
            sep = 2
        headers = req_bytes[:header_end].decode('utf-8', errors='replace')
        first_line = headers.splitlines()[0] if headers else ''
        print(f'  {first_line}')
