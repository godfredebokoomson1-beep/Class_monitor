package com.classmonitor.ui.controllers;

import com.classmonitor.domain.Student;
import com.classmonitor.repository.sqlite.SqliteStudentRepository;
import com.classmonitor.service.CsvService;
import com.classmonitor.service.ImportResult;
import com.classmonitor.service.StudentService;
import com.classmonitor.ui.AppNavigator;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;


import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import javafx.concurrent.Task;
import javafx.application.Platform;

public class ImportExportController {

    @FXML private Label importedLbl;
    @FXML private Label errorsLbl;
    @FXML private TextArea importLogArea;
    @FXML private Label statusLabel;

    @FXML private ProgressBar progressBar;
    @FXML private Label progressText;

    // Import uses your existing CSV service (kept)
    private final CsvService csv = new CsvService();

    // Export uses StudentService so we can write to a user-chosen file location
    private final StudentService studentService = new StudentService(new SqliteStudentRepository());

    // Tunables
    private static final double AT_RISK_GPA = 2.50;
    private static final double TOP_GPA = 3.50;

    // Navigation
    @FXML public void openDashboard(){ AppNavigator.goDashboard(); }
    @FXML public void openStudents(){ AppNavigator.goStudents(); }
    @FXML public void openReports(){ AppNavigator.goReports(); }
    @FXML public void openImportExport(){ AppNavigator.goImportExport(); }
    @FXML public void openSettings(){ AppNavigator.openSettingsModal(); }


    @FXML
    public void importCsv() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Students CSV");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));

        File f = fc.showOpenDialog(statusLabel.getScene().getWindow());
        if (f == null) return;

        statusLabel.setText("Importing...");
        importLogArea.clear();

        Task<ImportResult> task = new Task<>() {
            @Override
            protected ImportResult call() {
                return csv.importStudents(f);
            }
        };

        task.setOnSucceeded(e -> {
            ImportResult r = task.getValue();
            importedLbl.setText(String.valueOf(r.getSuccessCount()));
            errorsLbl.setText(String.valueOf(r.getFailureCount()));
            importLogArea.setText(r.getMessage());
            statusLabel.setText("Done");
        });

        task.setOnFailed(e -> statusLabel.setText("Failed: " + task.getException().getMessage()));

        Thread th = new Thread(task, "import-task");
        th.setDaemon(true);
        th.start();
    }

    @FXML
    public void exportAll() {
        File file = chooseSaveFile("Export All Students", "students_all.csv");
        if (file == null) return;

        runExportTask("export-all", "Exporting all...", () -> {
            List<Student> students = studentService.findAll();
            writeStudentsCsv(students, file);
            onExportSuccess(file);
        });
    }

    @FXML
    public void exportTop() {
        File file = chooseSaveFile("Export Top Students", "students_top.csv");
        if (file == null) return;

        runExportTask("export-top", "Exporting top...", () -> {
            List<Student> students = studentService.findAll().stream()
                    .filter(s -> s != null && s.gpa() >= TOP_GPA)
                    .collect(Collectors.toList());
            writeStudentsCsv(students, file);
            onExportSuccess(file);
        });
    }

    @FXML
    public void exportRisk() {
        File file = chooseSaveFile("Export At-Risk Students", "students_at_risk.csv");
        if (file == null) return;

        runExportTask("export-risk", "Exporting at-risk...", () -> {
            List<Student> students = studentService.findAll().stream()
                    .filter(s -> s != null && s.gpa() < AT_RISK_GPA)
                    .collect(Collectors.toList());
            writeStudentsCsv(students, file);
            onExportSuccess(file);
        });
    }

    // ---------------- helpers ----------------

    private File chooseSaveFile(String title, String defaultName) {
        FileChooser fc = new FileChooser();
        fc.setTitle(title);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));
        fc.setInitialFileName(defaultName);
        return fc.showSaveDialog(statusLabel.getScene().getWindow());
    }

    private void runExportTask(String threadName, String startingMessage, Runnable work) {
        Platform.runLater(() -> statusLabel.setText(startingMessage));

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                work.run();
                return null;
            }
        };

        task.setOnFailed(e -> Platform.runLater(() ->
                statusLabel.setText("Export failed: " + task.getException().getMessage())
        ));

        Thread th = new Thread(task, threadName);
        th.setDaemon(true);
        th.start();
        Task<Void> task1 = new Task<>() {
            @Override protected Void call() {
                updateMessage("Exporting...");
                updateProgress(0, 1);

                // do work...
                // updateProgress(0.5, 1);

                updateProgress(1, 1);
                updateMessage("Export complete ✅");
                return null;
            }
        };

        bindProgress(task);
        new Thread(task, "export-risk").start();


    }

    private void writeStudentsCsv(List<Student> students, File file) {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            out.write("studentId,fullName,programme,level,gpa,email,phone,enrolledDate,status");
            out.newLine();

            for (Student s : students) {
                out.write(csvCell(s.studentId())); out.write(',');
                out.write(csvCell(s.fullName()));  out.write(',');
                out.write(csvCell(s.programme())); out.write(',');
                out.write(String.valueOf(s.level())); out.write(',');
                out.write(String.valueOf(s.gpa())); out.write(',');
                out.write(csvCell(s.email())); out.write(',');
                out.write(csvCell(s.phone())); out.write(',');
                out.write(csvCell(s.enrolledDate())); out.write(',');
                out.write(csvCell(s.status()));
                out.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String csvCell(String v) {
        if (v == null) return "";
        boolean needsQuotes = v.contains(",") || v.contains("\n") || v.contains("\r") || v.contains("\"");
        String escaped = v.replace("\"", "\"\"");
        return needsQuotes ? ("\"" + escaped + "\"") : escaped;
    }

    private void onExportSuccess(File file) {
        // 1) Show full path in status label
        Platform.runLater(() -> statusLabel.setText("Exported to: " + file.getAbsolutePath()));

        // 2) Auto-open the file
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(file);
            }
        } catch (IOException ignored) {}

        // 3) Open folder in Explorer (select file when possible)
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop d = Desktop.getDesktop();

                if (d.isSupported(Desktop.Action.BROWSE_FILE_DIR)) {
                    d.browseFileDirectory(file);
                } else if (d.isSupported(Desktop.Action.OPEN) && file.getParentFile() != null) {
                    d.open(file.getParentFile());
                }
            }
        } catch (Exception ignored) {}
    }

    private void bindProgress(Task<?> task) {
        progressBar.visibleProperty().unbind();
        progressText.textProperty().unbind();

        progressBar.setVisible(true);
        progressBar.progressProperty().bind(task.progressProperty());
        progressText.textProperty().bind(task.messageProperty());

        task.setOnSucceeded(e -> {
            progressBar.progressProperty().unbind();
            progressText.textProperty().unbind();
            progressBar.setVisible(false);
            progressText.setText("");
        });

        task.setOnFailed(e -> {
            progressBar.progressProperty().unbind();
            progressText.textProperty().unbind();
            progressBar.setVisible(false);
            progressText.setText("");
        });
    }

}
