package com.classmonitor.service;

import com.classmonitor.ValidationException;
import com.classmonitor.domain.Student;
import com.classmonitor.util.AppLogger;
import com.classmonitor.repository.StudentRepository;
import java.util.List;
import com.classmonitor.domain.Student;



import java.time.LocalDate;

public final class StudentService {
    private final StudentRepository repo;

    public StudentService(StudentRepository repo) {
        this.repo = repo;
    }

    public List<Student> search(String q) {
        if (q == null || q.isBlank()) return repo.findAll();
        return repo.search(q.trim());
    }

    public void add(Student s) {
        validate(s, true);
        repo.add(s);
        AppLogger.log("ADD student_id=" + s.studentId());
    }

    public void update(Student s) {
        validate(s, false);
        repo.update(s);
        AppLogger.log("UPDATE student_id=" + s.studentId());
    }

    public void delete(String studentId) {
        if (studentId == null || studentId.isBlank()) throw new ValidationException("Student ID is required.");
        repo.delete(studentId.trim());
        AppLogger.log("DELETE student_id=" + studentId.trim());
    }

    public List<Student> findAll() {
        return repo.findAll();
    }

    public void validate(Student s, boolean isCreate) {
        if (s == null) throw new ValidationException("Student is required.");

        // Student ID: required, 4-20, letters/digits only, unique
        String id = safe(s.studentId());
        if (id.isEmpty()) throw new ValidationException("Student ID is required.");
        if (id.length() < 4 || id.length() > 20) throw new ValidationException("Student ID must be 4 to 20 characters.");
        if (!id.matches("^[A-Za-z0-9]+$")) throw new ValidationException("Student ID must contain letters and digits only.");
        if (isCreate && repo.existsById(id)) throw new ValidationException("Student ID already exists.");

        // Full name: required, 2-60, no digits
        String name = safe(s.fullName());
        if (name.length() < 2 || name.length() > 60) throw new ValidationException("Full name must be 2 to 60 characters.");
        if (name.matches(".*\\d.*")) throw new ValidationException("Full name must not contain digits.");

        // Programme: required
        if (safe(s.programme()).isEmpty()) throw new ValidationException("Programme is required.");

        // Level: one of 100..700
        int level = s.level();
        if (!(level == 100 || level == 200 || level == 300 || level == 400 || level == 500 || level == 600 || level == 700))
            throw new com.classmonitor.ValidationException("Level must be one of: 100, 200, 300, 400, 500, 600, 700.");

        // GPA: 0.0..5.0
        double gpa = s.gpa();
        if (gpa < 0.0 || gpa > 5.0) throw new com.classmonitor.ValidationException("GPA must be between 0.0 and 4.0.");

        // Email: contains @ and .
        String email = safe(s.email());
        if (!(email.contains("@") && email.contains("."))) throw new com.classmonitor.ValidationException("Email must contain @ and a dot.");

        // Phone: 10-15 digits only
        String phone = safe(s.phone());
        if (!phone.matches("^\\d{10,15}$")) throw new com.classmonitor.ValidationException("Phone number must be 10 to 15 digits (digits only).");


        // Date added: required ISO
        String dateIso = safe(s.dateAddedIso());
        if (dateIso.isEmpty()) throw new com.classmonitor.ValidationException("Date added is required.");
        try { LocalDate.parse(dateIso); } catch (Exception e) { throw new com.classmonitor.ValidationException("Date added must be a valid date (YYYY-MM-DD)."); }

        // Status: Active/Inactive
        String status = safe(s.status());
        if (!(status.equals("Active") || status.equals("Inactive")))
            throw new com.classmonitor.ValidationException("Status must be Active or Inactive.");

// normalize: keep only digits
        String digits = phone.replaceAll("\\D", "");

        if (!digits.isEmpty() && (digits.length() < 10 || digits.length() > 15)) {
            throw new ValidationException("Phone number must be 10 to 15 digits.");
        }


        if (!email.isBlank()) {
            // Strict but reasonable email pattern
            String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

            if (!email.matches(emailRegex)) {
                throw new ValidationException("Please enter a valid email address (e.g. user@gmail.com).");
            }
        }



    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    public void create(Student student) {
    }
    public boolean exists(String studentId) {
        return repo.findById(studentId) != null;
    }

}
