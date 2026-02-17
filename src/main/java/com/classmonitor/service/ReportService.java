package com.classmonitor.service;

import com.classmonitor.domain.Student;

import java.util.*;
import java.util.stream.Collectors;

public class ReportService {

    // TEMP in-memory data (replace with repository later)
    private final List<Student> students = new ArrayList<>();

    public ReportService() {
        // safe seed data so UI works
        students.add(new Student("UMAT001", "Alice Mensah", "Computer Science", 200, 3.5,
                "a@a.com", "0200000000", "2024-01-01", "Active"));
        students.add(new Student("UMAT002", "Yaw Boateng", "Electrical Eng", 300, 2.1,
                "b@b.com", "0200000001", "2024-01-01", "Active"));
    }

    // ✅ THIS IS THE MISSING METHOD
    public List<String> getProgrammes() {
        return students.stream()
                .map(Student::programme)
                .distinct()
                .sorted()
                .toList();
    }

    public List<Student> topPerformers(String programme, Integer level) {
        return students.stream()
                .filter(s -> programme == null || programme.equals(s.programme()))
                .filter(s -> level == null || level.equals(s.level()))
                .sorted(Comparator.comparingDouble(Student::gpa).reversed())
                .limit(10)
                .toList();
    }

    public List<Student> atRisk(double threshold) {
        return students.stream()
                .filter(s -> s.gpa() < threshold)
                .toList();
    }

    public Map<String, Long> programmeSummary() {
        return students.stream()
                .collect(Collectors.groupingBy(Student::programme, Collectors.counting()));
    }

    public Map<Integer, Long> gpaDistribution() {
        return students.stream()
                .collect(Collectors.groupingBy(
                        s -> (int) Math.floor(s.gpa()),
                        Collectors.counting()
                ));
    }

    // Table builders (safe stubs)
    public void buildTopPerformersTable(Object table, String p, Integer l) {}
    public void buildAtRiskTable(Object table, double t) {}
    public void buildGpaDistributionTable(Object table) {}
    public void buildProgrammeSummaryTable(Object table) {}
}
