package com.smalltiantian.sse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * 标准 SSE 协议解析类。
 *
 * @author tick
 */
public class SimpleSSEParse implements SSEParseNorm {
    private final static String EVENT = "message";

    private String lastId;
    private int retry;
    private BufferedReader bufferedReader = null;

    public SSEParseNorm setInputStream(InputStream is) {
        return this.setInputStream(is, "UTF-8");
    }

    @Override
    public SSEParseNorm setInputStream(InputStream is, String charsetName) {
        return this.setInputStream(is, Charset.forName(charsetName));
    }

    public SSEParseNorm setInputStream(InputStream is, Charset charsetName) {
        clear();

        this.bufferedReader = new BufferedReader(new InputStreamReader(is, charsetName));
        return this;
    }

    private void clear() {
        if (this.bufferedReader != null) {
            try {
                this.bufferedReader.close();
            } catch (IOException e) {
                this.bufferedReader = null;
            }
        }
    }

    @Override
    public void parse(SSECallback callback) throws IOException {
        if (this.bufferedReader == null) {
            throw new NullPointerException("You should call `setInputStream` method before this.");
        }
        callback.onStart();

        String line, header = null, body = null, event = null;
        StringBuilder sb = new StringBuilder();
        try {
            while ((line = this.bufferedReader.readLine()) != null) {
                if (line.isEmpty()) {
                    if (event != null) {
                        callback.doInEvent(event, sb.toString());
                        event = null;
                        sb.delete(0, sb.length());
                    }
                    continue;
                }

                // [field]: value\n
                header = line.substring(0, line.indexOf(":"));
                body = line.substring(line.indexOf(":") + ": ".length());

                if (header.equals("data")) {
                    if (event == null) event = EVENT;
                    sb.append(body);
                } else if (header.equals("event")) {
                    event = body;
                } else if (header.equals("id")) {
                    this.lastId = body;
                    callback.idChangedEvent(this.lastId);
                } else if (header.equals("retry")) {
                    this.retry = Integer.valueOf(body);

                    if (this.retry < 0) {
                        throw new RuntimeException("Retry time is " + this.retry + " < 0.");
                    }

                    callback.retryTimeEvent(this.retry);
                } else {
                    callback.commentEvent(body);
                }
            }
        } finally {
            callback.onClosed();
            this.bufferedReader.close();
        }
    }

    @Override
    public String lastId() {
        return this.lastId;
    }

    @Override
    public int retryTime() {
        return this.retry;
    }
}
