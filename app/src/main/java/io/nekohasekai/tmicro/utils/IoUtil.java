package io.nekohasekai.tmicro.utils;

import io.nekohasekai.tmicro.TMicro;
import javolution.io.UTF8StreamReader;
import javolution.io.UTF8StreamWriter;

import java.io.*;

public class IoUtil {

    public static final int DEFAULT_BUFFER_SIZE = 2 << 12;
    public static final int EOF = -1;

    public static void close(Reader reader) {
        try {
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void close(Writer reader) {
        try {
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void close(InputStream reader) {
        try {
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void close(OutputStream reader) {
        try {
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static long copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long size = 0;
        for (int readSize; (readSize = in.read(buffer)) != EOF; ) {
            out.write(buffer, 0, readSize);
            size += readSize;
            out.flush();
        }
        return size;
    }

    public static byte[] readByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copy(in, out);
        close(out);
        return out.toByteArray();
    }

    public static String readUTF8(InputStream in) throws IOException {
        UTF8StreamReader reader = new UTF8StreamReader().setInput(in);
        try {
            return read(reader);
        } finally {
            close(reader);
        }
    }

    public static String read(Reader reader) throws IOException {
        final StringBuffer builder = new StringBuffer();
        char[] buffer = new char[DEFAULT_BUFFER_SIZE];
        int size;
        while (true) {
            size = reader.read(buffer);
            if (size == -1) break;
            builder.append(buffer, 0, size);
        }
        return builder.toString();
    }

    public static InputStream getResIn(String path) throws IOException {
        if (!path.startsWith("/")) path = "/" + path;
        InputStream in = TMicro.class.getResourceAsStream(path);
        if (in == null) {
            throw new IOException(path + " not found");
        }
        return in;
    }

    public static byte[] readResBytes(String path) throws IOException {
        InputStream in = getResIn(path);
        try {
            return readByteArray(in);
        } finally {
            close(in);
        }
    }

    public static String readResUTF8(String path) throws IOException {
        InputStream in = getResIn(path);
        try {
            return readUTF8(in);
        } finally {
            close(in);
        }
    }

    public static void writeUTF8(OutputStream out, String str) throws IOException {
        UTF8StreamWriter writer = new UTF8StreamWriter().setOutput(out);
        writer.write(str);
        writer.flush();
    }

    public static ByteArrayOutputStream out() {
        return new ByteArrayOutputStream();
    }

    public static DataOutputStream open(OutputStream out) {
        return new DataOutputStream(out);
    }

}
