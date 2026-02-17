package com.classmonitor.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProgrammeDAO {

    private final Connection conn;

    public ProgrammeDAO(Connection conn) {
        this.conn = conn;
    }

    public List<String> getAllProgrammes() throws SQLException {
        List<String> out = new ArrayList<>();
        final String sql = "SELECT name FROM programmes ORDER BY name";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(rs.getString("name"));
            }
        }
        return out;
    }

    public void addProgramme(String name) throws SQLException {
        final String sql = "INSERT INTO programmes(name) VALUES(?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.trim());
            ps.executeUpdate();
        }
    }

    public void renameProgramme(String oldName, String newName) throws SQLException {
        final String sql = "UPDATE programmes SET name = ? WHERE name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newName.trim());
            ps.setString(2, oldName);
            ps.executeUpdate();
        }
    }

    public void deleteProgramme(String name) throws SQLException {
        final String sql = "DELETE FROM programmes WHERE name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.executeUpdate();
        }
    }
}
