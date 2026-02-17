package com.classmonitor.service;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public class SettingsService {
    private static final Path DATA_DIR = Paths.get("data");
    private static final Path FILE = DATA_DIR.resolve("settings.properties");
    private static final String KEY = "risk.threshold";
    private static final double DEFAULT = 2.0;

    public SettingsService() {
        ensureDataDir();
    }

    public double getRiskThreshold() {
        Properties p = load();
        try { return Double.parseDouble(p.getProperty(KEY, String.valueOf(DEFAULT))); }
        catch (Exception e) { return DEFAULT; }
    }

    public void setRiskThreshold(double value) {
        Properties p = load();
        p.setProperty(KEY, String.valueOf(value));
        save(p);
    }

    private Properties load() {
        Properties p = new Properties();
        if (Files.exists(FILE)) {
            try (InputStream in = Files.newInputStream(FILE)) { p.load(in); }
            catch (IOException ignored) {}
        }
        return p;
    }

    private void save(Properties p) {
        try (OutputStream out = Files.newOutputStream(FILE)) {
            p.store(out, "ClassMonitor settings");
        } catch (IOException ignored) {}
    }

    private void ensureDataDir() {
        try { Files.createDirectories(DATA_DIR); }
        catch (IOException ignored) {}
    }
}
