import json
import urllib.parse

har_path = r'c:\Users\罗宇轩\Desktop\campus(1)(4)\jw.swu.edu.cn_Archive [26-07-04 11-45-13].har'

with open(har_path, 'r', encoding='utf-8') as f:
    har = json.load(f)

entries = har['log']['entries']

# 1. 提取所有校区的楼号列表
print("=" * 70)
print("1. 各校区楼号列表")
print("=" * 70)

seen_xq = {}
for entry in entries:
    url = entry['request'].get('url', '')
    if 'cdjy_cxXqjc.html' not in url:
        continue

    parsed_url = urllib.parse.urlparse(url)
    qs = urllib.parse.parse_qs(parsed_url.query)
    xqh_id = qs.get('xqh_id', [''])[0]

    response_text = entry['response'].get('content', {}).get('text', '')
    if not response_text or xqh_id in seen_xq:
        continue

    seen_xq[xqh_id] = True
    try:
        data = json.loads(response_text)
        lh_list = data.get('lhList', [])
        print(f'\n校区 xqh_id={xqh_id}，共 {len(lh_list)} 栋楼：')
        for item in lh_list:
            jxldm = item.get('JXLDM', '')
            jxlmc = item.get('JXLMC', '')
            xq = item.get('XQH_ID', '')
            print(f'  {jxldm} -> {jxlmc}')
    except Exception as e:
        print(f'解析失败: {e}')

# 2. 空教室查询请求
print("\n" + "=" * 70)
print("2. 空教室查询请求参数")
print("=" * 70)

count = 0
for entry in entries:
    url = entry['request'].get('url', '')
    if 'cdjy_cxKxcdlb.html' not in url or 'doType=query' not in url:
        continue

    count += 1
    method = entry['request'].get('method', '')
    print(f'\n--- 请求 #{count}: {method} {url} ---')

    post_data = entry['request'].get('postData', {})
    text = post_data.get('text', '')
    params = post_data.get('params', [])

    if params:
        for p in params:
            print(f"  {p['name']} = {p.get('value', '')}")
    elif text:
        parsed = urllib.parse.parse_qs(text)
        for k, v in sorted(parsed.items()):
            print(f'  {k} = {v[0] if len(v) == 1 else v}')

# 3. cdjy_cxKtjc.html 响应
print("\n" + "=" * 70)
print("3. cdjy_cxKtjc.html 响应")
print("=" * 70)

for entry in entries:
    url = entry['request'].get('url', '')
    if 'cdjy_cxKtjc.html' not in url:
        continue

    print(f'\nURL: {url}')
    response_text = entry['response'].get('content', {}).get('text', '')
    if response_text:
        print(response_text[:2000])
        print(f'\n响应总长度: {len(response_text)}')
