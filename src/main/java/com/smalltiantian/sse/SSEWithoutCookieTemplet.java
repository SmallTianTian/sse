package com.smalltiantian.sse;

import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SSEWithoutCookieTemplet {
    private static OkHttpClient CLIENT = null;

    private Request request;
    private SSEParseNorm sseParse = new SimpleSSEParse();
    private Exception exception = null;

    public SSEWithoutCookieTemplet(Request request) {
        if (request == null) throw new NullPointerException("callback == null");

        this.request = request;
    }

    public SSEWithoutCookieTemplet(String getMethodForUrl) {
        this(new Request.Builder().url(getMethodForUrl).build());
    }

    /**
     * 程序运行过程中可能的错误。
     *
     * @return 运行错误
     */
    public final Exception exeption() {
        return this.exception;
    }

    /**
     * 设置将服务端返回的数据解析为事件的处理类 {@link SSEParseNorm}。<p>
     *
     * 如果不设置将采用系统默认的解析类 {@link SimpleSSEParse}。<br>
     *
     * <strong>若服务端返回 SSE 标准的数据，不建议用户设置此项。</strong>
     *
     * @param  sseParse 用户自定义的事件处理类
     * @return 自身
     */
    public final SSEWithoutCookieTemplet setSSEParse(SSEParseNorm sseParse) {
        if (sseParse == null) throw new NullPointerException("sseParse == null");

        this.sseParse = sseParse;
        return this;
    }

    Thread cancelCallThread = null;
    private volatile boolean isRunning = false;
    private volatile Call currentCall = null;

    /**
     * 执行程序。
     *
     * 向目标地址请求数据，
     * 若目标地址返回的不是 SSE 协议（header("Content-Type") != "text/event-stream"），将抛出 {@code IllegalAccessError}。<p>
     *
     * 程序会根据 SSE 协议中的 'retry' 时间重新发起请求，一般情况下不需要调用者额外处理此部分信息。<p>
     *
     * 程序执行过程中出现 3 次以内的错误将 保存错误信息 并 自动重试，保证程序的高可用性：<br>
     * 1. 自动重试期间能正常工作，程序将重置 重试次数 及 异常信息，<br>
     * 2. 自动重试期间还是无法正常工作，程序将终止运行，建议调用 {@link #exception} 获取异常信息。
     *
     * @param callback 事件处理类
     */
    public final void execute(final SSECallback callback) {
        if (callback == null) throw new NullPointerException("callback == null");

        isRunning = true;
        callback.onStart();
        for (int faildTime = 0; faildTime < 3; faildTime++) {
            Response response = null;
            try {
                currentCall = prepareCall();
                response = currentCall.execute();

                if (!isSSEProtocol(response)) {
                    response.close();
                    throw new IllegalAccessError("This not a sse connection.");
                }

                this.sseParse.setInputStream(response.body().byteStream(), getCharsetFromResponse(response, "utf-8"));

                SSECallback systemCall = new SystemCallback(this, callback);
                this.sseParse.parse(systemCall);
            } catch (Exception e) {
                if (("Socket closed".equals(e.getMessage()) || "Canceled".equals(e.getMessage())) && currentCall.isCanceled()) {
                    faildTime = 0;
                    exception = null;
                } else {
                    e.printStackTrace();
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e1) {}

                    exception = e;
                }
            } finally {
                if (response != null && response.body() != null)
                    response.close();
            }
        }
        isRunning = false;
        callback.onClosed();
    }

    /**
     * 准备 {@link Call}。
     *
     * 由于每次请求会附带不同的数据，所以需要每次得到返回值前提供新的 {@link Call}。
     *
     * @return 添加指定数据后的 {@link Call}
     */
    protected Call prepareCall() {
        return prepareCallWithoutCookie();
    }

    /**
     * 根据 SSE 标准添加头部信息 {@code Last-Event-ID}。
     *
     * @return {@link Call}
     */
    private final Call prepareCallWithoutCookie() {
        Request request = null;

        // add last id
        if (this.sseParse.lastId() != null && !this.sseParse.lastId().isEmpty()) {
            request = this.request.newBuilder()
                          .addHeader("Last-Event-ID", this.sseParse.lastId())
                          .build();
        }
        return getCilent().newCall(request == null ? this.request : request);
    }

    /**
     * 根据响应判断是否是 SSE 协议。
     *
     * @param  response 服务器返回数据
     * @return 是否是 SSE 协议
     */
    protected final static boolean isSSEProtocol(Response response) {
        String content_type = response.header("Content-Type").split(";")[0];
        return content_type.toLowerCase().equalsIgnoreCase("text/event-stream");
    }

    /**
     * 获取服务器返回编码集。
     *
     * @param  response 服务端返回数据
     * @param  defaultCharset 默认编码集
     * @return 数据编码集
     */
    protected final static String getCharsetFromResponse(Response response, String defaultCharset) {
        String charset = "charset";
        if (response.header(charset) != null) {
            return response.header(charset);
        }
        String decodeSource = response.header("Content-Type");
        int start, end;
        if ((start = decodeSource.indexOf(charset)) > 0) {
            end = decodeSource.indexOf(";", start);
            return decodeSource.substring(start + charset.length() + 1, end == -1 ? decodeSource.length() : end);
        }
        return defaultCharset;
    }

    /**
     * 获取 {@link OkHttpClient}。
     *
     * @return {@link OkHttpClient}
     */
    private static OkHttpClient getCilent() {
        if (CLIENT == null) {
            CLIENT = new OkHttpClient().newBuilder()
                                       .readTimeout(0, TimeUnit.MILLISECONDS)
                                       .build();
        }
        return CLIENT;
    }

    /**
     * 根据 `retry` 提供指定时间自动重试服务。
     *
     * @author tick
     */
    static class SystemCallback extends SSECallbackDefaultAdapter {
        private final SSECallback userCallBack;
        private final SSEWithoutCookieTemplet templet;

        private SystemCallback(SSEWithoutCookieTemplet templet, SSECallback userCallBack) {
            this.templet      = templet;
            this.userCallBack = userCallBack;
        }

        @Override
        public void idChangedEvent(String id) {
            this.userCallBack.idChangedEvent(id);
        }

        @Override
        public void retryTimeEvent(int retryTime) {
            if (templet.cancelCallThread != null && !templet.cancelCallThread.isInterrupted()) {
                templet.cancelCallThread.interrupt();
            }

            Runnable cancelCall = new Runnable() {
                @Override
                public void run() {
                    while (templet.isRunning) {
                        try {
                            long cc = TimeUnit.MILLISECONDS.convert(retryTime, TimeUnit.SECONDS);
                            Thread.sleep(cc);
                        } catch (InterruptedException e) {
                            // stupid
                            if ("sleep interrupted".equals(e.getMessage())) return;
                        }
                        templet.currentCall.cancel();
                    }
                }
            };
            templet.cancelCallThread = new Thread(cancelCall);
            templet.cancelCallThread.start();

            userCallBack.retryTimeEvent(retryTime);
        }

        @Override
        public void commentEvent(String comment) {
            this.userCallBack.commentEvent(comment);
        }

        @Override
        public void doInEvent(String event, String data) {
            this.userCallBack.doInEvent(event, data);
        }
    }
}