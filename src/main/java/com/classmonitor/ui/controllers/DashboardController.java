package com.classmonitor.ui.controllers;

import com.classmonitor.repository.StudentRepository;
import com.classmonitor.repository.sqlite.SqliteStudentRepository;
import com.classmonitor.service.StudentService;
import com.classmonitor.ui.AppNavigator;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

public class DashboardController {

    @FXML private Label totalLbl;
    @FXML private Label activeLbl;
    @FXML private Label inactiveLbl;
    @FXML private Label avgGpaLbl;

    @FXML private PieChart gpaPie;
    @FXML private BarChart<String, Number> gpaBar;

    private final StudentService service = new StudentService(new SqliteStudentRepository());

    @FXML


    private void loadAnalytics() {
        var students = service.findAll();

        long atRisk = students.stream().filter(s -> s.gpa() < 2.5).count();
        long mid = students.stream().filter(s -> s.gpa() >= 2.5 && s.gpa() < 3.5).count();
        long top = students.stream().filter(s -> s.gpa() >= 3.5).count();

        gpaPie.getData().setAll(
                new PieChart.Data("At Risk (<2.5)", atRisk),
                new PieChart.Data("Average (2.5–3.49)", mid),
                new PieChart.Data("Top (>=3.5)", top)
        );

        var series = new XYChart.Series<String, Number>();
        series.getData().add(new XYChart.Data<>("At Risk", atRisk));
        series.getData().add(new XYChart.Data<>("Average", mid));
        series.getData().add(new XYChart.Data<>("Top", top));

        gpaBar.getData().setAll(series);
    }


    private final StudentRepository repo = new SqliteStudentRepository();

    @FXML
    public void initialize() {
        refreshDashboard();
    }

    private void refreshDashboard() {
        var all = repo.findAll();
        long total = all.size();
        long active = all.stream().filter(s -> "Active".equalsIgnoreCase(s.status())).count();
        long inactive = total - active;
        double avg = all.stream().mapToDouble(s -> s.gpa()).average().orElse(0.0);

        totalLbl.setText(String.valueOf(total));
        activeLbl.setText(String.valueOf(active));
        inactiveLbl.setText(String.valueOf(inactive));
        avgGpaLbl.setText(String.format("%.2f", avg));
    }

    @FXML public void openDashboard(){ AppNavigator.goDashboard(); }
    @FXML public void openStudents(){ AppNavigator.goStudents(); }
    @FXML public void openReports(){ AppNavigator.goReports(); }
    @FXML public void openImportExport(){ AppNavigator.goImportExport(); }



    @FXML
    public void openSettings() {
        AppNavigator.openSettingsModal();
        // When modal closes, refresh in case settings influence dashboard counts later
        refreshDashboard();
    }


}
