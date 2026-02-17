package com.classmonitor.domain;

/**
 * Core domain model for a student.
 * Used by UI, services, reports, and CSV import/export.
 */
public record Student(
        String studentId,
        String fullName,
        String programme,
        int level,
        double gpa,
        String email,
        String phone,
        String enrolledDate,
        String status
) {
    /**
     * Convenience no-arg constructor for places that expect it.
     * Records must delegate to the canonical constructor.
     */
    public Student() {
        this("", "", "", 0, 0.0, "", "", "", "Active");
    }

    // Compatibility alias (some parts of the project expect this name)
    public String dateAddedIso() {
        return enrolledDate;
    }

    // Optional: if any code expects "dateEnrolled()"
    public String dateEnrolled() {
        return enrolledDate;
    }
}
