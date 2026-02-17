package com.classmonitor.repository;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class Schema {

    private Schema() {}

    public static void ensure(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {

            // ---- Core table: students (REQUIRED) ----
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS students (
                  student_id TEXT PRIMARY KEY,
                  full_name TEXT NOT NULL,
                  programme TEXT NOT NULL,
                  level INTEGER NOT NULL,
                  gpa REAL NOT NULL,
                  email TEXT,
                  phone TEXT,
                  date_added TEXT,
                  status TEXT NOT NULL
                )
            """);

            // ---- Settings table ----
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS app_settings (
                  key TEXT PRIMARY KEY,
                  value TEXT NOT NULL
                )
            """);

            st.executeUpdate("""
                INSERT OR IGNORE INTO app_settings(key, value)
                VALUES ('at_risk_gpa_threshold', '2.50')
            """);

            // ---- Programmes table (optional feature) ----
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS programmes (
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  name TEXT NOT NULL UNIQUE
                )
            """);
        }
    }
}
