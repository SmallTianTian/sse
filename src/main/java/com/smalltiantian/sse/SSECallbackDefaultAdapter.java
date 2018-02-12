package com.smalltiantian.sse;

public class SSECallbackDefaultAdapter implements SSECallback {
    public void onStart() {}
    public void idChangedEvent(String id) {}
    public void retryTimeEvent(int retryTime) {}
    public void commentEvent(String comment) {}
    public void doInEvent(String event, String data) {}
    public void onClosed() {}
}
