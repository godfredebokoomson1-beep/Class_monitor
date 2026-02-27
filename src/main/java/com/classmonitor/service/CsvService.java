package com.classmonitor.service;

import com.classmonitor.domain.Student;
import com.classmonitor.repository.sqlite.SqliteStudentRepository;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

public class CsvService {

    private static final Path DATA_DIR = Paths.get("data");

    private final SqliteStudentRepository repo = new SqliteStudentRepository();

    public CsvService() {
        try { Files.createDirectories(DATA_DIR); } catch (Exception ignored) {}
    }

    /* =========================
       IMPORT (REAL)
       ========================= */
    public ImportResult importStudents(File file) {

        int success = 0;
        int failed = 0;
        StringBuilder log = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {

            String headerLine = br.readLine();
            if (headerLine == null) {
                return new ImportResult(0, 1, "Empty CSV file.");
            }

            headerLine = headerLine.replace("\uFEFF", "").trim(); // remove BOM
            String delimiter = detectDelimiter(headerLine);

            Map<String, Integer> header = parseHeader(headerLine, delimiter);

            // required columns (minimum)
            require(header, "studentid");
            require(header, "fullname");
            require(header, "programme");
            require(header, "level");
            require(header, "gpa");

            String line;
            int rowNum = 1;

            while ((line = br.readLine()) != null) {
                rowNum++;
                if (line.trim().isEmpty()) continue;

                try {
                    List<String> cols = splitCsvLine(line, delimiter);

                    String studentId = get(cols, header, "studentid");
                    String fullName  = get(cols, header, "fullname");
                    String programme = get(cols, header, "programme");
                    String levelTxt  = get(cols, header, "level");
                    String gpaTxt    = get(cols, header, "gpa");
                    String email     = optional(cols, header, "email");
                    String phone     = optional(cols, header, "phone");
                    String date      = optional(cols, header, "date", LocalDate.now().toString());
                    String status    = optional(cols, header, "status", "Active");

                    // ---- Validation (same style as your app) ----
                    if (studentId.isBlank()) throw new RuntimeException("Student ID is required.");
                    if (fullName.isBlank())  throw new RuntimeException("Full name is required.");
                    if (programme.isBlank()) throw new RuntimeException("Programme is required.");

                    int level;
                    try { level = Integer.parseInt(levelTxt.trim()); }
                    catch (Exception e) { throw new RuntimeException("Level must be a number (e.g. 100,200...)."); }

                    double gpa;
                    try { gpa = Double.parseDouble(gpaTxt.trim()); }
                    catch (Exception e) { throw new RuntimeException("GPA must be a number (e.g. 3.10)."); }

                    if (!email.isBlank()) {
                        if (!email.contains("@") || !email.contains(".")) {
                            throw new RuntimeException("Email must contain @ and a dot.");
                        }
                    }

                    if (!phone.isBlank()) {
                        if (!phone.matches("\\d{10,15}")) {
                            throw new RuntimeException("Phone number must be 10 to 15 digits (digits only).");
                        }
                    }

                    Student s = new Student(
                            studentId.trim(),
                            fullName.trim(),
                            programme.trim(),
                            level,
                            gpa,
                            email.trim(),
                            phone.trim(),
                            date.trim(),
                            status.isBlank() ? "Active" : status.trim()
                    );

                    // UPSERT (update if exists, else add)
                    if (repo.existsById(s.studentId())) repo.update(s);
                    else repo.add(s);

                    success++;

                } catch (Exception ex) {
                    failed++;
                    log.append("Row ").append(rowNum)
                            .append(": ").append(ex.getMessage())
                            .append(" | Data: ").append(line)
                            .append("\n");
                }
            }

        } catch (Exception e) {
            return new ImportResult(0, 1, "Failed to read CSV: " + e.getMessage());
        }

        if (log.isEmpty()) log.append("Import completed successfully.");
        return new ImportResult(success, failed, log.toString());
    }

    /* =========================
       EXPORT (REAL)
       ========================= */
    public void exportAllStudents() {
        List<Student> students = repo.findAll();
        writeStudentsCsv(DATA_DIR.resolve("students_all.csv"), students);
    }

    /* =========================
       INTERNAL HELPERS
       ========================= */

    private static String detectDelimiter(String headerLine) {
        int commas = count(headerLine, ',');
        int semis  = count(headerLine, ';');
        return (semis > commas) ? ";" : ",";
    }

    private static int count(String s, char c) {
        int n = 0;
        for (int i = 0; i < s.length(); i++) if (s.charAt(i) == c) n++;
        return n;
    }

    private static Map<String, Integer> parseHeader(String headerLine, String delimiter) {
        List<String> cols = splitCsvLine(headerLine, delimiter);
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < cols.size(); i++) {
            String key = cols.get(i).trim().toLowerCase().replace(" ", "");
            map.put(key, i);
        }
        return map;
    }

    private static void require(Map<String, Integer> header, String key) {
        if (!header.containsKey(key)) {
            throw new RuntimeException("Missing required column in header: " + key);
        }
    }

    private static String get(List<String> cols, Map<String, Integer> header, String key) {
        Integer idx = header.get(key);
        if (idx == null || idx >= cols.size()) return "";
        return cols.get(idx).trim();
    }

    private static String optional(List<String> cols, Map<String, Integer> header, String key) {
        return get(cols, header, key);
    }

    private static String optional(List<String> cols, Map<String, Integer> header, String key, String def) {
        String v = get(cols, header, key);
        return v.isBlank() ? def : v;
    }

    // Handles commas/semicolons with basic quotes support
    private static List<String> splitCsvLine(String line, String delimiter) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        char delim = delimiter.charAt(0);

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') {
                inQuotes = !inQuotes;
                continue;
            }

            if (!inQuotes && ch == delim) {
                out.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(ch);
            }
        }
        out.add(cur.toString());
        return out;
    }

    private void writeStudentsCsv(Path out, List<Student> students) {
        try (BufferedWriter bw = Files.newBufferedWriter(out)) {
            bw.write("studentId,fullName,programme,level,gpa,email,phone,date,status\n");
            for (Student s : students) {
                bw.write(String.join(",",
                        esc(s.studentId()),
                        esc(s.fullName()),
                        esc(s.programme()),
                        String.valueOf(s.level()),
                        String.valueOf(s.gpa()),
                        esc(s.email()),
                        esc(s.phone()),
                        esc(s.dateEnrolled()),
                        esc(s.status())
                ));
                bw.newLine();
            }
        } catch (Exception ignored) {}
    }

    private String esc(String v) {
        if (v == null) return "";
        String t = v.trim();
        if (t.contains(",") || t.contains("\"")) {
            t = t.replace("\"", "\"\"");
            return "\"" + t + "\"";
        }
        return t;
    }


}
