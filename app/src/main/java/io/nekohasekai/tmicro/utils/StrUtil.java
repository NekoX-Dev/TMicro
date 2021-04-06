package io.nekohasekai.tmicro.utils;

import java.io.UnsupportedEncodingException;

public class StrUtil {

    public static String UTF_8 = "UTF-8";

    public static String str(byte[] bytes) {
        try {
            return new String(bytes, UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return new String(bytes);
        }
    }

    public static byte[] bytes(String str) {
        try {
            return str.getBytes(UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return str.getBytes();
        }
    }

}
