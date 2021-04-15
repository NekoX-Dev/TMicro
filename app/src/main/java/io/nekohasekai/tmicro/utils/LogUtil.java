package io.nekohasekai.tmicro.utils;

import io.nekohasekai.tmicro.TMicro;

public class LogUtil {

    public static void info(String message) {
        if (TMicro.DEBUG) {
            System.out.println(message);
        }
    }

    public static String getClassName(Object obj) {
        String className = obj.getClass().getName();
        className = className.substring(className.lastIndexOf('.') + 1);
        return className;
    }

    public static String formatError(Throwable error) {
        StringBuffer message = new StringBuffer();
        message.append("Error: ");
        message.append(getClassName(error));
        if (error.getMessage() != null && error.getMessage().length() > 0) {
            message.append(" - ");
            message.append(error.getMessage());
        }
        return message.toString();
    }

    public static void warn(String message) {
        if (TMicro.DEBUG) {
            try {
                // if don't throw, there is no stacktrace to print
                throw new Exception("Error: " + message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void error(Throwable error, String message) {
        if (TMicro.DEBUG) {
            warn(message);
            error.printStackTrace();
        }
    }

}
