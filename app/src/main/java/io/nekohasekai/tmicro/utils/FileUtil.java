package io.nekohasekai.tmicro.utils;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

public class FileUtil {

    public static String PROTOCOL = "file://";

    private static String defaultDir;

    public static String getDefaultDir() {
        if (defaultDir == null) {
            Enumeration enumeration = FileSystemRegistry.listRoots();
            if (!enumeration.hasMoreElements()) throw new IllegalStateException("No storage found");
            defaultDir = (String) enumeration.nextElement();
        }
        return defaultDir;
    }

    public static FileConnection getFile(String path) throws IOException {
        return (FileConnection) Connector.open(PROTOCOL + "/" + getDefaultDir() + "/NekoX/" + path);
    }

    public static FileConnection getFile(FileConnection parent, String path) throws IOException {
        return (FileConnection) Connector.open(parent.getURL() + path);
    }

    public static String[] listRoots() {
        Enumeration enumeration = FileSystemRegistry.listRoots();
        Vector list = new Vector();
        while (enumeration.hasMoreElements()) {
            list.addElement(enumeration.nextElement());
        }
        String[] roots = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            roots[i] = (String) list.elementAt(i);
        }
        return roots;
    }

}
