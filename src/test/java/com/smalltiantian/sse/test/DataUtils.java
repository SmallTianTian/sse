package com.smalltiantian.sse.test;

import java.util.Map;
import java.util.HashMap;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.ByteArrayInputStream;
import java.io.BufferedReader;

public class DataUtils {
    private static final File TEXT = new File(System.getProperty("user.dir") + "/src/test/resource/SSENormText.txt");
    private static final Map<String, String> dataMap = new HashMap<String, String>();

    static {
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            fis = new FileInputStream(System.getProperty("user.dir") + "/src/test/resource/EventContentMap.csv");
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
            // skip first line
            String msg = br.readLine();
            while ((msg = br.readLine()) != null) {
                String[] fields = msg.split(",");
                dataMap.put(fields[1].isEmpty() ? "message" : fields[1], fields[2]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
                isr.close();
                fis.close();
            } catch (Exception e) {
                br = null;
                isr = null;
                fis = null;
            }
        }
    }

    public static InputStream getUTF8InputStream() {
        try {
            return new FileInputStream(TEXT);
        } catch (Exception e) {
            return null;
        }
    }

    public static InputStream getGBKInputStream() {
        InputStream is = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(TEXT);
            byte[] b = new byte[fis.available()];
            fis.read(b);
            String middle = new String(b);
            is = new ByteArrayInputStream(middle.getBytes("GBK"));
        } catch (Exception e) {}
        finally {
            try {
                fis.close();
            } catch (Exception e) {
                fis = null;
            }
        }
        return is;
    }

    public static Map<String, String> getDataMap() {
        return new HashMap<>(dataMap);
    }

    private DataUtils() {
        throw new AssertionError();
    }
}
