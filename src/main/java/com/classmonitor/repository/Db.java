package com.classmonitor.repository;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public final class Db {

    private static Connection conn;

    private Db() {}

    public static Connection get() throws SQLException {
        if (conn == null || conn.isClosed()) {

            // 🔹 Open SQLite connection (keep your DB name/path as-is)
            conn = DriverManager.getConnection("jdbc:sqlite:classmonitor.db");

            // 🔹 ENSURE required tables exist (settings + programmes)
            Schema.ensure(conn);
        }
        return conn;
    }

    public static void initSchema() {
        try {
            // Ensure connection exists
            Connection c = get();

            // Ensure tables exist
            Schema.ensure(c);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize DB schema: " + e.getMessage(), e);
        }
    }

    public static Connection getConnection() {
        return null;
    }
}
