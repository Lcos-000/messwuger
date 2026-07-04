import http.server
import socketserver
import json
import sys
import io

LOG_FILE = r"c:\Users\罗宇轩\Desktop\campus(1)(4)\mock_java_server.log"

class Tee:
    def __init__(self, *files):
        self.files = files
    def write(self, obj):
        for f in self.files:
            f.write(obj)
            f.flush()
    def flush(self):
        for f in self.files:
            f.flush()

sys.stdout = Tee(sys.stdout, io.open(LOG_FILE, "a", encoding="utf-8"))

class Handler(http.server.BaseHTTPRequestHandler):
    def do_POST(self):
        print("=" * 60)
        print("Received callback:", self.path)
        print("Headers:", dict(self.headers))
        length = int(self.headers.get("Content-Length", 0))
        body = self.rfile.read(length)
        print("Body:")
        try:
            print(json.dumps(json.loads(body), ensure_ascii=False, indent=2))
        except Exception:
            print(body.decode("utf-8", errors="ignore"))
        self.send_response(200)
        self.end_headers()
        self.wfile.write(b"ok")

    def log_message(self, *args):
        pass

httpd = socketserver.TCPServer(("127.0.0.1", 18000), Handler)
print("Mock Java server listening on http://127.0.0.1:18000")
httpd.serve_forever()
