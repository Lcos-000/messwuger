"""Run the fixed DES helper without putting credentials in argv."""

import json
import os
import subprocess


_NODE_DES_ENCRYPTOR = r"""
const fs = require('fs');
let input = '';
process.stdin.setEncoding('utf8');
process.stdin.on('data', chunk => { input += chunk; });
process.stdin.on('end', () => {
  try {
    const payload = JSON.parse(input);
    const code = fs.readFileSync(payload.scriptPath, 'utf8');
    eval(code);
    process.stdout.write(strEnc(payload.data, payload.key, "", "") + "\n");
  } catch (error) {
    console.error(error && error.stack ? error.stack : String(error));
    process.exitCode = 1;
  }
});
"""


def des_encrypt(data: str, key: str, des_js_path: str) -> str:
    """Encrypt data with des.js while keeping dynamic values out of argv."""
    if not os.path.exists(des_js_path):
        raise RuntimeError("des.js 不存在")

    payload = json.dumps(
        {"data": data, "key": key, "scriptPath": os.path.abspath(des_js_path)},
        ensure_ascii=False,
    )
    result = subprocess.run(
        ["node", "-e", _NODE_DES_ENCRYPTOR],
        input=payload,
        capture_output=True,
        text=True,
        timeout=15,
    )
    if result.returncode != 0:
        raise RuntimeError(f"Node.js DES 加密失败: {result.stderr}")
    return result.stdout.strip()
