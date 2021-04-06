package io.nekohasekai.tmicro.utils;

import io.nekohasekai.tmicro.TMicro;
import j2me.util.HashMap;
import javolution.io.UTF8StreamReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public class ResUtil {

    private static final String INVALID_RESOURCE_FORMAT
            = "The resource file format is invalid.";
    private static final String ERROR_LOADING_RESOURCES
            = "Error loading resources.";

    public static HashMap readPropertiesRes(String path) {
        try {
            InputStream in = TMicro.class.getResourceAsStream(path);
            if (in == null) {
                throw new IOException(path + " not found");
            }

            return readProperties(new UTF8StreamReader().setInput(in));
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    public static HashMap readProperties(Reader reader) throws IOException {
        HashMap properties = new HashMap();
        StringBuffer buffer = new StringBuffer();

        try {
            int letter = -1;
            while ((letter = reader.read()) != -1) {
                switch (letter) {
                    case '\r':
                    case '\n':
                        break;
                    default:
                        buffer.append((char) letter);
                        break;
                }

                if (letter == '\n') {
                    String line = buffer.toString();
                    int separator = line.indexOf("=");
                    if (separator < 0) {
                        throw new IOException(INVALID_RESOURCE_FORMAT);
                    }
                    String key = line.substring(0x00, separator);
                    String value = line.substring(++separator, line.length());
                    properties.put(key, value);
                    buffer.delete(0x00, buffer.length());
                }
            }
        } catch (Exception e) {
            throw new IOException(ERROR_LOADING_RESOURCES);
        }
        return properties;
    }

}
