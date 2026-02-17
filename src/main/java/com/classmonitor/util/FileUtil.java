package com.classmonitor.util;

import java.io.File;

public final class FileUtil {
    private FileUtil(){}

    public static void ensureDataFolder() {
        File f = new File("data");
        if (!f.exists()) f.mkdirs();
    }
}
