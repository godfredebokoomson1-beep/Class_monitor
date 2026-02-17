package com.classmonitor.service;

import com.classmonitor.ValidationException;
import com.classmonitor.domain.Student;
import com.classmonitor.repository.sqlite.SqliteStudentRepository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class CsvService {

    private final StudentService service =
            new StudentService(new SqliteStudentRepository());

    public ImportResult importStudents(File file) {

        int success = 0;
        int failure = 0;
        StringBuilder log = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {

            String header = br.readLine();
            if (header == null || !header.toLowerCase().contains("studentid")) {
                return new ImportResult(0, 1,
                        "Invalid CSV header. Expected studentId,fullName,programme,...");
            }

            String line;
            int rowNumber = 1;

            while ((line = br.readLine()) != null) {
                rowNumber++;

                try {
                    String[] parts = line.split(",", -1);

                    if (parts.length < 9) {
                        throw new ValidationException("Row has missing columns.");
                    }

                    Student student = new Student(
                            parts[0].trim(),                // studentId
                            parts[1].trim(),                // fullName
                            parts[2].trim(),                // programme
                            Integer.parseInt(parts[3].trim()),  // level
                            Double.parseDouble(parts[4].trim()),// gpa
                            parts[5].trim(),                // email
                            parts[6].trim(),                // phone
                            parts[7].trim(),                // enrolledDate
                            parts[8].trim().isBlank() ? "Active" : parts[8].trim() // status
                    );

                    // Check if student exists → update instead of insert
                    if (service.exists(student.studentId())) {
                        service.update(student);
                    } else {
                        service.add(student);
                    }

                    success++;

                } catch (Exception ex) {
                    failure++;
                    log.append("Row ")
                            .append(rowNumber)
                            .append(": ")
                            .append(ex.getMessage())
                            .append("\n");
                }
            }

        } catch (Exception e) {
            return new ImportResult(0, 1,
                    "Failed to read file: " + e.getMessage());
        }

        return new ImportResult(success, failure, log.toString());
    }
}
