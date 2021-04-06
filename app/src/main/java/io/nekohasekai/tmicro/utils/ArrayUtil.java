package io.nekohasekai.tmicro.utils;

public class ArrayUtil {

    public static byte[] sub(byte[] src, int len) {
        byte[] dist = new byte[len];
        copy(src, 0, dist, 0, len);
        return dist;
    }

    public static byte[] sub(byte[] src, int offset, int len) {
        byte[] dist = new byte[len];
        copy(src, offset, dist, 0, len);
        return dist;
    }

    public static Object copy(Object src, int srcPos, Object dest, int destPos, int length) {
        //noinspection SuspiciousSystemArraycopy
        System.arraycopy(src, srcPos, dest, destPos, length);
        return dest;
    }

    public static Object copy(Object src, Object dest, int length) {
        //noinspection SuspiciousSystemArraycopy
        System.arraycopy(src, 0, dest, 0, length);
        return dest;
    }

}
