package com.smalltiantian.sse.test;

import com.smalltiantian.sse.*;

import java.util.Map;

import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.MediaType;

import org.junit.Test;
import org.junit.Assert;

public class SSEWithoutCookieTempletTest {

    @Test
    public void testConnectionWithGetUTF8() {
        String url = "http://localhost:8888/sse_without_cookie";
        Request request = new Request.Builder()
                                     .url(url)
                                     .addHeader("Accept-Charset", "UTF-8")
                                     .build();

        final Map<String, String> dataMap = DataUtils.getDataMap();
        final boolean[] flag = new boolean[1];
        flag[0] = false;

        SSEWithoutCookieTemplet templet = new SSEWithoutCookieTemplet(request);

        templet.execute(new SSECallbackDefaultAdapter() {
            @Override
            public void doInEvent(String event, String content) {
                flag[0] = true;
                Assert.assertEquals(content, dataMap.get(event));
            }
        });

        Assert.assertTrue(flag[0]);
    }

    @Test
    public void testConnectionWithGetGBK() {
        String url = "http://localhost:8888/sse_without_cookie";
        Request request = new Request.Builder()
                                     .url(url)
                                     .addHeader("Accept-Charset", "GBK")
                                     .build();

        final Map<String, String> dataMap = DataUtils.getDataMap();
        final boolean[] flag = new boolean[1];
        flag[0] = false;

        SSEWithoutCookieTemplet templet = new SSEWithoutCookieTemplet(request);

        templet.execute(new SSECallbackDefaultAdapter() {
            @Override
            public void doInEvent(String event, String content) {
                flag[0] = true;
                Assert.assertEquals(content, dataMap.get(event));
            }
        });

        Assert.assertTrue(flag[0]);
    }

    @Test
    public void testConnectionWithPostUTF8() {
        String url = "http://localhost:8888/sse_without_cookie";
        Request request = new Request.Builder()
                                     .url(url)
                                     .addHeader("Accept-Charset", "GBK")
                                     .post(RequestBody.create(MediaType.parse("String"), ""))
                                     .build();

        final Map<String, String> dataMap = DataUtils.getDataMap();
        final boolean[] flag = new boolean[1];
        flag[0] = false;

        SSEWithoutCookieTemplet templet = new SSEWithoutCookieTemplet(request);

        templet.execute(new SSECallbackDefaultAdapter() {
            @Override
            public void doInEvent(String event, String content) {
                flag[0] = true;
                Assert.assertEquals(content, dataMap.get(event));
            }
        });

        Assert.assertTrue(flag[0]);
    }
}
