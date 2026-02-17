package com.classmonitor.util;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;

public final class AppLogger {
    private static final String LOG_FILE = "data/app.log";

    private AppLogger(){}

    public static void init() {
        com.classmonitor.util.FileUtil.ensureDataFolder();
        log("LOGGER_READY");
    }

    public static void log(String msg) {
        // Do NOT log full records (privacy rule)
        try(PrintWriter out = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            out.println(LocalDateTime.now() + " | " + msg);
        } catch (Exception ignored) {}
    }
}
