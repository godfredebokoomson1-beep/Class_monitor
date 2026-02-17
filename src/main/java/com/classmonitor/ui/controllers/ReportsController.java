package com.classmonitor.ui.controllers;

import com.classmonitor.domain.Student;
import com.classmonitor.repository.Db;
import com.classmonitor.repository.SettingsDAO;
import com.classmonitor.repository.sqlite.SqliteStudentRepository;
import com.classmonitor.ui.AppNavigator;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;

//herfhhh'rh'838qkurgj hrgh jgb rg/JBR  gbkjb
//ueg  r.guguvhr rghwrg 858rer8hr 8rHGH
// OREGEBN NIET
// RGERKBN
// wufhsk GUB;gigkG;gus;u84Y 8HGA.I8;HUHGO8TH.URGKHDOYO8O52

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportsController {

    @FXML private Label lblTotal;
    @FXML private Label lblAtRisk;
    @FXML private Label lblPercent;
    @FXML private Label lblThreshold;

    @FXML private TableView<Student> riskTable;
    @FXML private TableColumn<Student, String> colId;
    @FXML private TableColumn<Student, String> colName;
    @FXML private TableColumn<Student, String> colProgramme;
    @FXML private TableColumn<Student, Double> colGpa;

    @FXML private TableView<Map.Entry<String, Long>> programmeTable;
    @FXML private TableColumn<Map.Entry<String, Long>, String> colProgName;
    @FXML private TableColumn<Map.Entry<String, Long>, Long> colProgCount;

    private final SqliteStudentRepository repo = new SqliteStudentRepository();
    private double threshold = 2.50;

    @FXML
    public void initialize() {
        // Empty states (polish)
        riskTable.setPlaceholder(new Label("No students currently below the GPA threshold."));
        programmeTable.setPlaceholder(new Label("No at-risk data to summarise yet."));

        // Tooltips (polish)
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
            lblThreshold.setText(String.format(java.util.Locale.US, "%.2f", threshold));
        } catch (Exception e) {
            threshold = 2.50;
            lblThreshold.setText("2.50");
        }
    }

    private void setupTables() {
        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().studentId()));
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().fullName()));
        colProgramme.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().programme()));
        colGpa.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().gpa()));

        colProgName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getKey()));
        colProgCount.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getValue()));
    }

    private void setupRowAndGpaStyling() {
        // Row highlight for at-risk students
        riskTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Student item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                    return;
                }
                if (item.gpa() < threshold) {
                    setStyle("-fx-background-color: rgba(255, 0, 0, 0.12);");
                } else {
                    setStyle("");
                }
            }
        });

        // GPA color cues
        colGpa.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double gpa, boolean empty) {
                super.updateItem(gpa, empty);
                if (empty || gpa == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                setText(String.format(java.util.Locale.US, "%.2f", gpa));

                if (gpa < threshold) {
                    setStyle("-fx-text-fill: #b42318; -fx-font-weight: bold;");
                } else if (gpa < threshold + 0.5) {
                    setStyle("-fx-text-fill: #b26a00;");
                } else {
                    setStyle("-fx-text-fill: #1e7f43;");
                }
            }
        });
    }

    private void loadReport() {
        List<Student> all = repo.findAll();

        List<Student> atRisk = all.stream()
                .filter(s -> s.gpa() < threshold)
                .toList();

        lblTotal.setText(String.valueOf(all.size()));
        lblAtRisk.setText(String.valueOf(atRisk.size()));

        double percent = all.isEmpty() ? 0 : (atRisk.size() * 100.0 / all.size());
        lblPercent.setText(String.format(java.util.Locale.US, "%.1f%%", percent));

        riskTable.setItems(FXCollections.observableArrayList(atRisk));

        Map<String, Long> byProgramme = atRisk.stream()
                .collect(Collectors.groupingBy(Student::programme, Collectors.counting()));

        ObservableList<Map.Entry<String, Long>> progRows =
                FXCollections.observableArrayList(byProgramme.entrySet());

        programmeTable.setItems(progRows);

        // force style refresh when data changes
        riskTable.refresh();
    }

    // NAVIGATION
    @FXML public void openDashboard(){ AppNavigator.goDashboard(); }
    @FXML public void openStudents(){ AppNavigator.goStudents(); }
    @FXML public void openReports(){ AppNavigator.goReports(); }
    @FXML public void openImportExport(){ AppNavigator.goImportExport(); }
    @FXML public void openSettings(){ AppNavigator.openSettingsModal(); }

}
