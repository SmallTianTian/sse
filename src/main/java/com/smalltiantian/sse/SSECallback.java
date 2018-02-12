package com.smalltiantian.sse;

public interface SSECallback {
    /**
     * 程序开始事件。
     */
    void onStart();

    /**
     * Id 变化事件。
     * @param id 服务端 Id
     */
    void idChangedEvent(String id);

    /**
     * 间隔请求的时间变化事件。
     *
     * @param retryTime 间隔时间
     */
    void retryTimeEvent(int retryTime);

    /**
     * comment 事件。
     *
     * 一般为了保持连接的存活。
     *
     * @param comment comment
     */
    void commentEvent(String comment);

    /**
     * 数据事件。
     *
     * @param event 事件类型
     * @param data 事件数据
     */
    void doInEvent(String event, String data);

    /**
     * 程序完成事件。
     */
    void onClosed();
}
