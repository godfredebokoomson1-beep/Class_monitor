package com.classmonitor.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SettingsDAO {

    private static final String KEY_AT_RISK = "at_risk_gpa_threshold";
    private final Connection conn;

    public SettingsDAO(Connection conn) {
        this.conn = conn;
    }

    public double getAtRiskThreshold() {
        final String sql = "SELECT value FROM app_settings WHERE key = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, KEY_AT_RISK);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Double.parseDouble(rs.getString("value"));
                }
            }
        } catch (Exception ignored) {
        }
        return 2.50; // fallback default
    }

    public void setAtRiskThreshold(double value) throws SQLException {
        // SQLite UPSERT
        final String sql =
                "INSERT INTO app_settings(key, value) VALUES(?, ?) " +
                        "ON CONFLICT(key) DO UPDATE SET value = excluded.value";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, KEY_AT_RISK);
            ps.setString(2, String.format(java.util.Locale.US, "%.2f", value));
            ps.executeUpdate();
        }
    }
}
