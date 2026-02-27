package com.classmonitor.ui.controllers;

import com.classmonitor.domain.Student;
import com.classmonitor.repository.Db;
import com.classmonitor.repository.SettingsDAO;
import com.classmonitor.repository.sqlite.SqliteStudentRepository;
import com.classmonitor.ui.AppNavigator;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;

public class ReportsController {

    @FXML private Label lblTotal;
    @FXML private Label lblAtRisk;
    @FXML private Label lblPercent;
    @FXML private Label lblThreshold;

    // Existing: At risk table
    @FXML private TableView<Student> riskTable;
    @FXML private TableColumn<Student, String> colId;
    @FXML private TableColumn<Student, String> colName;
    @FXML private TableColumn<Student, String> colProgramme;
    @FXML private TableColumn<Student, Double> colGpa;

    // Existing: programme breakdown
    @FXML private TableView<Map.Entry<String, Long>> programmeTable;
    @FXML private TableColumn<Map.Entry<String, Long>, String> colProgName;
    @FXML private TableColumn<Map.Entry<String, Long>, Long> colProgCount;

    // NEW: Top performers
    @FXML private TableView<Student> topTable;
    @FXML private TableColumn<Student, String> topColId;
    @FXML private TableColumn<Student, String> topColName;
    @FXML private TableColumn<Student, String> topColProgramme;
    @FXML private TableColumn<Student, Double> topColGpa;

    // NEW: Active list
    @FXML private TableView<Student> activeTable;
    @FXML private TableColumn<Student, String> actColId;
    @FXML private TableColumn<Student, String> actColName;
    @FXML private TableColumn<Student, String> actColProgramme;
    @FXML private TableColumn<Student, Double> actColGpa;

    // NEW: Inactive list
    @FXML private TableView<Student> inactiveTable;
    @FXML private TableColumn<Student, String> inColId;
    @FXML private TableColumn<Student, String> inColName;
    @FXML private TableColumn<Student, String> inColProgramme;
    @FXML private TableColumn<Student, Double> inColGpa;

    // NEW: Pie chart
    @FXML private PieChart statusPie;

    private final SqliteStudentRepository repo = new SqliteStudentRepository();
    private double threshold = 2.50;

    @FXML
    public void initialize() {
        // placeholders
        riskTable.setPlaceholder(new Label("No students currently below the GPA threshold."));
        programmeTable.setPlaceholder(new Label("No at-risk data to summarise yet."));
        if (topTable != null) topTable.setPlaceholder(new Label("No students found."));
        if (activeTable != null) activeTable.setPlaceholder(new Label("No active students found."));
        if (inactiveTable != null) inactiveTable.setPlaceholder(new Label("No inactive students found."));

        lblThreshold.setTooltip(new Tooltip("Configured in Settings → At-Risk GPA Threshold"));
        lblAtRisk.setTooltip(new Tooltip("Students with GPA below the configured threshold"));

        loadThreshold();
        setupTables();
        setupRowAndGpaStyling();
        loadReport();
    }

    private void loadThreshold() {
        try {
            Connection conn = Db.get();
            threshold = new SettingsDAO(conn).getAtRiskThreshold();
            lblThreshold.setText(String.format(Locale.US, "%.2f", threshold));
        } catch (Exception e) {
            threshold = 2.50;
            lblThreshold.setText("2.50");
        }
    }

    private void setupTables() {
        // existing risk
        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().studentId()));
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().fullName()));
        colProgramme.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().programme()));
        colGpa.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().gpa()));

        // programme breakdown
        colProgName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getKey()));
        colProgCount.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getValue()));

        // top performers
        if (topTable != null) {
            topColId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().studentId()));
            topColName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().fullName()));
            topColProgramme.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().programme()));
            topColGpa.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().gpa()));
        }

        // active
        if (activeTable != null) {
            actColId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().studentId()));
            actColName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().fullName()));
            actColProgramme.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().programme()));
            actColGpa.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().gpa()));
        }

        // inactive
        if (inactiveTable != null) {
            inColId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().studentId()));
            inColName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().fullName()));
            inColProgramme.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().programme()));
            inColGpa.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().gpa()));
        }
    }

    private void setupRowAndGpaStyling() {
        // Row highlight for at-risk students
        riskTable.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(Student item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setStyle(""); return; }
                if (item.gpa() < threshold) setStyle("-fx-background-color: rgba(255, 0, 0, 0.12);");
                else setStyle("");
            }
        });

        // GPA color cues for risk table
        colGpa.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double gpa, boolean empty) {
                super.updateItem(gpa, empty);
                if (empty || gpa == null) { setText(null); setStyle(""); return; }
                setText(String.format(Locale.US, "%.2f", gpa));
                if (gpa < threshold) setStyle("-fx-text-fill: #b42318; -fx-font-weight: bold;");
                else if (gpa < threshold + 0.5) setStyle("-fx-text-fill: #b26a00;");
                else setStyle("-fx-text-fill: #1e7f43;");
            }
        });
    }

    private void loadReport() {
        List<Student> all = repo.findAll();

        // at-risk
        List<Student> atRisk = all.stream()
                .filter(s -> s.gpa() < threshold)
                .sorted(Comparator.comparingDouble(Student::gpa))
                .toList();

        lblTotal.setText(String.valueOf(all.size()));
        lblAtRisk.setText(String.valueOf(atRisk.size()));

        double percent = all.isEmpty() ? 0 : (atRisk.size() * 100.0 / all.size());
        lblPercent.setText(String.format(Locale.US, "%.1f%%", percent));

        riskTable.setItems(FXCollections.observableArrayList(atRisk));

        // programme breakdown (at-risk)
        Map<String, Long> byProgramme = atRisk.stream()
                .collect(Collectors.groupingBy(Student::programme, Collectors.counting()));
        programmeTable.setItems(FXCollections.observableArrayList(byProgramme.entrySet()));

        // top performers (top 10)
        if (topTable != null) {
            List<Student> top = all.stream()
                    .sorted(Comparator.comparingDouble(Student::gpa).reversed())
                    .limit(10)
                    .toList();
            topTable.setItems(FXCollections.observableArrayList(top));
        }

        // active/inactive lists
        List<Student> active = new ArrayList<>();
        List<Student> inactive = new ArrayList<>();

        for (Student s : all) {
            String status = safeStatusOf(s);
            if ("inactive".equalsIgnoreCase(status)) inactive.add(s);
            else active.add(s);
        }

        if (activeTable != null) activeTable.setItems(FXCollections.observableArrayList(active));
        if (inactiveTable != null) inactiveTable.setItems(FXCollections.observableArrayList(inactive));

        // pie chart
        if (statusPie != null) {
            ObservableList<PieChart.Data> pie = FXCollections.observableArrayList(
                    new PieChart.Data("Active", active.size()),
                    new PieChart.Data("Inactive", inactive.size())
            );
            statusPie.setData(pie);
            statusPie.setLegendVisible(true);
        }

        riskTable.refresh();
    }

    /**
     * Reads student status without forcing your Student class to have a specific method at compile time.
     * Supports:
     *  - String status()
     *  - boolean isActive()
     *  - Boolean active()
     */
    private String safeStatusOf(Student s) {
        try {
            // try status(): String
            Method m = s.getClass().getMethod("status");
            Object v = m.invoke(s);
            return v == null ? "active" : v.toString();
        } catch (Exception ignored) {}

        try {
            // try isActive(): boolean
            Method m = s.getClass().getMethod("isActive");
            Object v = m.invoke(s);
            if (v instanceof Boolean b) return b ? "active" : "inactive";
        } catch (Exception ignored) {}

        try {
            // try active(): Boolean
            Method m = s.getClass().getMethod("active");
            Object v = m.invoke(s);
            if (v instanceof Boolean b) return b ? "active" : "inactive";
        } catch (Exception ignored) {}

        return "active"; // fallback
    }

    // NAVIGATION (unchanged)
    @FXML public void openDashboard(){ AppNavigator.goDashboard(); }
    @FXML public void openStudents(){ AppNavigator.goStudents(); }
    @FXML public void openReports(){ AppNavigator.goReports(); }
    @FXML public void openImportExport(){ AppNavigator.goImportExport(); }
    @FXML public void openSettings(){ AppNavigator.openSettingsModal(); }
}
