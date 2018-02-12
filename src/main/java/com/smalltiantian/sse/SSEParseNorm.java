package com.smalltiantian.sse;

import java.io.IOException;
import java.io.InputStream;

public interface SSEParseNorm {
    /**
     * 解析服务端数据生成相应的事件并传递给 事件处理类。
     *
     * @param  callback 事件处理类
     * @throws IOException 解析过程中出现的 IO 错误
     */
    void parse(SSECallback callback) throws IOException;

    /**
     * 返回数据中的重试时间。
     *
     * 默认 0。
     * @return 重试时间
     */
    int retryTime();

    /**
     * 返回数据中最后一次 Id 值。
     *
     * 默认 null。
     * @return 最后一次 Id 值
     */
    String lastId();

    /**
     * 设置服务端的返回数据流。
     *
     * @param  is 服务端返回数据流
     * @param  charset 数据流编码集
     * @return 自身
     */
    SSEParseNorm setInputStream(InputStream is, String charset);
}
