package com.classmonitor.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SettingsDAO {

    private final Connection conn;

    public SettingsDAO(Connection conn) {
        this.conn = conn;
        ensureRowExists();
    }

    private void ensureRowExists() {
        ensureTable();
        ensureColumns();
        insertDefaultRow();
    }

    private void ensureTable() {
        String sql = """
        CREATE TABLE IF NOT EXISTS settings (
            id INTEGER PRIMARY KEY,
            at_risk_threshold REAL DEFAULT 2.0,
            average_threshold REAL DEFAULT 3.0,
            top_threshold REAL DEFAULT 3.5
        )
        """;
        try (var st = conn.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void ensureColumns() {
        // If your table existed before without new columns, add them safely
        addColumnIfMissing("average_threshold", "REAL DEFAULT 3.0");
        addColumnIfMissing("top_threshold", "REAL DEFAULT 3.5");
        addColumnIfMissing("at_risk_threshold", "REAL DEFAULT 2.0");
    }

    private void addColumnIfMissing(String column, String ddl) {
        try (var rs = conn.createStatement().executeQuery("PRAGMA table_info(settings)")) {
            while (rs.next()) {
                String name = rs.getString("name");
                if (column.equalsIgnoreCase(name)) return;
            }
        } catch (SQLException e) {
            // if PRAGMA fails, let it throw later
        }

        String sql = "ALTER TABLE settings ADD COLUMN " + column + " " + ddl;
        try (var st = conn.createStatement()) {
            st.execute(sql);
        } catch (SQLException ignored) {
            // ignore if already exists or table just created with it
        }
    }

    private void insertDefaultRow() {
        try (var ps = conn.prepareStatement(
                "INSERT OR IGNORE INTO settings (id, at_risk_threshold, average_threshold, top_threshold) VALUES (1, 2.0, 3.0, 3.5)"
        )) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    // ======================
    // AT RISK
    // ======================

    public double getAtRiskThreshold() {
        return getValue("at_risk_threshold", 2.0);
    }

    public void setAtRiskThreshold(double value) {
        updateValue("at_risk_threshold", value);
    }

    // ======================
    // AVERAGE
    // ======================

    public double getAverageThreshold() {
        return getValue("average_threshold", 3.0);
    }

    public void setAverageThreshold(double value) {
        updateValue("average_threshold", value);
    }

    // ======================
    // TOP
    // ======================

    public double getTopThreshold() {
        return getValue("top_threshold", 3.5);
    }

    public void setTopThreshold(double value) {
        updateValue("top_threshold", value);
    }

    // ======================
    // INTERNAL HELPERS
    // ======================

    private double getValue(String column, double defaultValue) {
        String sql = "SELECT " + column + " FROM settings WHERE id = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
            return defaultValue;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateValue(String column, double value) {
        String sql = "UPDATE settings SET " + column + " = ? WHERE id = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, value);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
