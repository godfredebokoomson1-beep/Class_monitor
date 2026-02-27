package com.classmonitor.ui.controllers;

import com.classmonitor.repository.StudentRepository;
import com.classmonitor.repository.sqlite.SqliteStudentRepository;
import com.classmonitor.ui.AppNavigator;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

import java.util.Map;
import java.util.TreeMap;

public class DashboardController {

    @FXML private Label totalLbl;
    @FXML private Label activeLbl;
    @FXML private Label inactiveLbl;
    @FXML private Label avgGpaLbl;

    @FXML private PieChart gpaPie;
    @FXML private BarChart<String, Number> gpaBar;

    private final StudentRepository repo = new SqliteStudentRepository();

    @FXML
    public void initialize() {
        loadDashboard();
    }

    private void loadDashboard() {

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {

                var all = repo.findAll();

                long total = all.size();
                long active = all.stream().filter(s -> "Active".equalsIgnoreCase(s.status())).count();
                long inactive = total - active;
                double avg = all.stream().mapToDouble(s -> s.gpa()).average().orElse(0.0);

                // GPA Distribution buckets
                long atRisk = all.stream().filter(s -> s.gpa() < 2.5).count();
                long mid = all.stream().filter(s -> s.gpa() >= 2.5 && s.gpa() < 3.5).count();
                long top = all.stream().filter(s -> s.gpa() >= 3.5).count();

                // GPA by Level (average GPA per level)
                Map<Integer, double[]> sums = new TreeMap<>(); // level -> [sum, count]
                for (var s : all) {
                    sums.computeIfAbsent(s.level(), k -> new double[]{0.0, 0.0});
                    sums.get(s.level())[0] += s.gpa();
                    sums.get(s.level())[1] += 1.0;
                }

                Platform.runLater(() -> {
                    // Stats
                    totalLbl.setText(String.valueOf(total));
                    activeLbl.setText(String.valueOf(active));
                    inactiveLbl.setText(String.valueOf(inactive));
                    avgGpaLbl.setText(String.format("%.2f", avg));

                    // Pie: GPA Distribution
                    gpaPie.getData().setAll(
                            new PieChart.Data("At Risk (<2.5)", atRisk),
                            new PieChart.Data("Average (2.5–3.49)", mid),
                            new PieChart.Data("Top (≥3.5)", top)
                    );

                    // Bar: Avg GPA by Level
                    gpaBar.getData().clear();
                    var series = new XYChart.Series<String, Number>();
                    series.setName("Avg GPA");

                    for (var e : sums.entrySet()) {
                        int level = e.getKey();
                        double sum = e.getValue()[0];
                        double count = e.getValue()[1];
                        double avgLevelGpa = (count == 0) ? 0.0 : (sum / count);

                        series.getData().add(new XYChart.Data<>(String.valueOf(level), avgLevelGpa));
                    }

                    gpaBar.getData().add(series);
                });

                return null;
            }
        };

        Thread t = new Thread(task, "dashboard-analytics");
        t.setDaemon(true);
        t.start();
    }

    @FXML public void openStudents(){ AppNavigator.goStudents(); }
    @FXML public void openReports(){ AppNavigator.goReports(); }
    @FXML public void openImportExport(){ AppNavigator.goImportExport(); }

    @FXML
    public void openSettings() {
        AppNavigator.openSettingsModal();
        loadDashboard(); // refresh after settings closes
    }
}
