package com.classmonitor.repository.sqlite;

import com.classmonitor.domain.Student;
import com.classmonitor.repository.Db;
import com.classmonitor.repository.StudentRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class SqliteStudentRepository implements StudentRepository {

    @Override
    public boolean existsById(String studentId) {
        String sql = "SELECT 1 FROM students WHERE student_id = ?";
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            throw new RuntimeException("existsById failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void add(Student s) {
        String sql = """
            INSERT INTO students(student_id, full_name, programme, level, gpa, email, phone, date_added, status)
            VALUES(?,?,?,?,?,?,?,?,?)
            """;
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            fill(ps, s);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("add failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Student s) {
        String sql = """
            UPDATE students
            SET full_name=?, programme=?, level=?, gpa=?, email=?, phone=?, status=?
            WHERE student_id=?
            """;
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, s.fullName());
            ps.setString(2, s.programme());
            ps.setInt(3, s.level());
            ps.setDouble(4, s.gpa());
            ps.setString(5, s.email());
            ps.setString(6, s.phone());
            ps.setString(7, s.status());
            ps.setString(8, s.studentId());

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("update failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String studentId) {
        String sql = "DELETE FROM students WHERE student_id = ?";
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("delete failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Student> findById(String studentId) {
        String sql = "SELECT * FROM students WHERE student_id = ?";
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(map(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException("findById failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Student> findAll() {
        String sql = "SELECT * FROM students ORDER BY full_name ASC";
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Student> out = new ArrayList<>();
            while (rs.next()) out.add(map(rs));
            return out;

        } catch (Exception e) {
            throw new RuntimeException("findAll failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Student> search(String query) {
        String sql = """
            SELECT * FROM students
            WHERE student_id LIKE ? OR LOWER(full_name) LIKE ?
            ORDER BY full_name ASC
            """;
        String q = "%" + query.trim().toLowerCase() + "%";

        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, "%" + query.trim() + "%");
            ps.setString(2, q);

            try (ResultSet rs = ps.executeQuery()) {
                List<Student> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }

        } catch (Exception e) {
            throw new RuntimeException("search failed: " + e.getMessage(), e);
        }
    }

    private static void fill(PreparedStatement ps, Student s) throws SQLException {
        ps.setString(1, s.studentId());
        ps.setString(2, s.fullName());
        ps.setString(3, s.programme());
        ps.setInt(4, s.level());
        ps.setDouble(5, s.gpa());
        ps.setString(6, s.email());
        ps.setString(7, s.phone());
        ps.setString(8, s.dateAddedIso());
        ps.setString(9, s.status()); // ✅ fixed
    }

    private static Student map(ResultSet rs) throws SQLException {
        return new Student(
                rs.getString("student_id"),
                rs.getString("full_name"),
                rs.getString("programme"),
                rs.getInt("level"),
                rs.getDouble("gpa"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("date_added"),
                rs.getString("status")
        );
    }


}
