#coding=utf-8
from wsgiref.simple_server import make_server
import time
import sys

def sse_content():
    file_name = sys.path[0] + '/SSENormText.txt'
    with open(file_name, 'r') as f:
        return f.readlines()

sse_content = sse_content()

def application(environ, start_response):
    if environ['PATH_INFO'] == '/sse_without_cookie':
        return sse_without_cookie(environ, start_response)
    start_response('200 OK',[('Content-Type','text/html')])
    return '<h1>Hello, web! </h1>'

def sse_without_cookie(environ, start_response):
    charset = environ['HTTP_ACCEPT_CHARSET'] if environ.has_key('HTTP_ACCEPT_CHARSET') else 'UTF-8'
    start_response('200 OK',[('Content-Type','text/event-stream'), ('charset', charset)])
    last_event_id = 'id: ' + environ['HTTP_LAST_EVENT_ID'] + '\n' if environ.has_key('HTTP_LAST_EVENT_ID') else None
    start_in_content = sse_content.index(last_event_id) if last_event_id else 0
    content = sse_content[start_in_content:]
    for item in content:
        time.sleep(1)
        yield item.decode('utf-8').encode(charset)

if __name__ == '__main__':
    httpd = make_server('', 8888, application)
    print("Serving HTTP on port 8888...")
    # 开始监听HTTP请求:
    httpd.serve_forever()
