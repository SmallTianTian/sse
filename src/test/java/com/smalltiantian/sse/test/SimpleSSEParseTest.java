package com.smalltiantian.sse.test;

import com.smalltiantian.sse.*;

import java.util.Map;
import java.io.InputStream;

import org.junit.Test;
import org.junit.Assert;

public class SimpleSSEParseTest {

    @Test
    public void testInputStreamWithUTF8() throws Exception {
        final String[] lastId = new String[1];
        final int[] lastRetryTime = new int[1];
        final Map<String, String> data = DataUtils.getDataMap();

        InputStream in = DataUtils.getUTF8InputStream();
        SimpleSSEParse listener = new SimpleSSEParse();
        listener.setInputStream(in);

        SSECallback callback = new SSECallbackDefaultAdapter() {
            @Override
            public void doInEvent(String event, String content) {
                Assert.assertEquals(data.get(event), content);
            }

            @Override
            public void idChangedEvent(String id) {
                Assert.assertNotNull(id);
                lastId[0] = id;
            }

            @Override
            public void retryTimeEvent(int retryTime) {
                Assert.assertTrue(retryTime > 0);
                lastRetryTime[0] = retryTime;
            }
        };

        listener.parse(callback);

        Assert.assertEquals(listener.lastId(), lastId[0]);
        Assert.assertEquals(listener.retryTime(), lastRetryTime[0]);
    }

    @Test
    public void testInputStreamWithGBK() throws Exception {
        final boolean[] flag = new boolean[1];
        final Map<String, String> data = DataUtils.getDataMap();
        flag[0] = false;

        InputStream in = DataUtils.getGBKInputStream();
        SimpleSSEParse listener = new SimpleSSEParse();
        listener.setInputStream(in, "GBK");

        SSECallback callback = new SSECallbackDefaultAdapter() {
            @Override
            public void doInEvent(String event, String content) {
                flag[0] = true;
                Assert.assertEquals(data.get(event), content);
            }
        };

        listener.parse(callback);
        Assert.assertTrue(flag[0]);
    }
}
