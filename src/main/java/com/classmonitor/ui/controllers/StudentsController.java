package com.classmonitor.ui.controllers;

import com.classmonitor.ValidationException;
import com.classmonitor.domain.Student;
import com.classmonitor.repository.sqlite.SqliteStudentRepository;
import com.classmonitor.service.StudentService;
import com.classmonitor.ui.AppNavigator;
import com.classmonitor.ui.models.StudentRow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import com.classmonitor.service.StudentService;

public class StudentsController {

    @FXML private TextField searchField;
    @FXML private Label statusLabel;

    @FXML private TableView<StudentRow> table;
    @FXML private TableColumn<StudentRow, String> colId;
    @FXML private TableColumn<StudentRow, String> colName;
    @FXML private TableColumn<StudentRow, String> colProgramme;
    @FXML private TableColumn<StudentRow, Number> colLevel;
    @FXML private TableColumn<StudentRow, Number> colGpa;
    @FXML private TableColumn<StudentRow, String> colEmail;
    @FXML private TableColumn<StudentRow, String> colPhone;
    @FXML private TableColumn<StudentRow, String> colStatus;

    @FXML private TextField idField;
    @FXML private TextField nameField;
    @FXML private ComboBox<String> programmeCombo;   // ✅ matches students.fxml
    @FXML private ComboBox<Integer> levelCombo;
    @FXML private TextField gpaField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<String> statusCombo;

    private final SqliteStudentRepository repo = new SqliteStudentRepository(); // ✅ no-arg constructor
    private final StudentService service = new StudentService(repo);
    private final ObservableList<StudentRow> rows = FXCollections.observableArrayList();

    private double atRiskThreshold = 2.50;

    @FXML
    public void initialize() {
        // table bindings
        colId.setCellValueFactory(d -> d.getValue().studentIdProperty());
        colName.setCellValueFactory(d -> d.getValue().fullNameProperty());
        colProgramme.setCellValueFactory(d -> d.getValue().programmeProperty());
        colLevel.setCellValueFactory(d -> d.getValue().levelProperty());
        colGpa.setCellValueFactory(d -> d.getValue().gpaProperty());
        colEmail.setCellValueFactory(d -> d.getValue().emailProperty());
        colPhone.setCellValueFactory(d -> d.getValue().phoneProperty());
        colStatus.setCellValueFactory(d -> d.getValue().statusProperty());

        table.setItems(rows);

        levelCombo.setItems(FXCollections.observableArrayList(100, 200, 300, 400));
        statusCombo.setItems(FXCollections.observableArrayList("Active", "Inactive", "Graduated"));

        // ✅ allow typing in programme
        programmeCombo.setEditable(true);

        // selection -> fill form
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected == null) return;

            idField.setText(selected.getStudentId());
            nameField.setText(selected.getFullName());
            programmeCombo.getEditor().setText(selected.getProgramme()); // ✅ typed/selected
            levelCombo.setValue(selected.getLevel());
            gpaField.setText(String.valueOf(selected.getGpa()));
            emailField.setText(selected.getEmail());
            phoneField.setText(selected.getPhone());
            statusCombo.setValue(selected.getStatus());

        });

        refresh();
    }



    @FXML
    private void addStudent() {
        try {
            Student s = buildStudent(true);
            service.add(s);
            setStatus("Student added successfully.", false);
            clearForm();
            refresh();
        } catch (ValidationException ve) {
            setStatus(ve.getMessage(), true); // show validation error nicely
        } catch (Exception e) {
            setStatus("Unexpected error: " + e.getMessage(), true);
        }
    }



    @FXML
    private void updateStudent() {
        Student s = buildStudent(false);
        service.update(s);
        setStatus("Student updated.", false);
        clearForm();
        refresh();
    }

    @FXML
    private void deleteStudent() { // or deleteStudent(), whichever your FXML uses
        try {
            String id = idField.getText();
            if (id == null || id.isBlank()) {
                setStatus("Select a student to delete.", true);
                return;
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Delete");
            alert.setHeaderText("Delete this student?");
            alert.setContentText("Student ID: " + id + "\nThis action cannot be undone.");

            var result = alert.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) return;

            service.delete(id.trim());
            setStatus("Student deleted.", false);
            clearForm();
            refresh();
        } catch (Exception e) {
            setStatus("Delete failed: " + e.getMessage(), true);
        }
    }


    @FXML
    private void clear() {
        clearForm();
        table.getSelectionModel().clearSelection();
        setStatus("", false);
    }

    @FXML
    private void search() {
        refresh();
    }

    @FXML public void openDashboard(){ AppNavigator.goDashboard(); }
    @FXML public void openReports(){ AppNavigator.goReports(); }
    @FXML public void openImportExport(){ AppNavigator.goImportExport(); }
    @FXML public void openSettings(){ AppNavigator.openSettingsModal(); }


    @FXML
    private void doSearch() {
        refresh();
    }
    @FXML
    public void openStudents() {
        AppNavigator.goStudents();
    }




    private void refresh() {
        rows.clear();

        var list = (searchField == null || searchField.getText().isBlank())
                ? service.findAll()
                : service.search(searchField.getText().trim());

        for (Student s : list) {
            rows.add(toRow(s));
        }
    }

    private Student buildStudent(boolean isCreate) {
        String id = idField.getText();
        String name = nameField.getText();
        String programme = programmeCombo.getEditor().getText(); // ✅ works for typing
        Integer level = levelCombo.getValue();
        String gpaTxt = gpaField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String status = statusCombo.getValue();

        if (id == null || id.isBlank()) throw new ValidationException("Student ID is required.");
        if (name == null || name.isBlank()) throw new ValidationException("Full name is required.");
        if (programme == null || programme.isBlank()) throw new ValidationException("Programme is required.");
        if (level == null) throw new ValidationException("Level is required.");
        if (gpaTxt == null || gpaTxt.isBlank()) throw new ValidationException("GPA is required.");

        double gpa;
        try { gpa = Double.parseDouble(gpaTxt.trim()); }
        catch (Exception e) { throw new ValidationException("GPA must be a number (0.0 - 4.0)."); }

        String dateIso = LocalDate.now().toString();

        return new Student(
                id.trim(),
                name.trim(),
                programme.trim(),
                level,
                gpa,
                email == null ? "" : email.trim(),
                phone == null ? "" : phone.trim(),
                dateIso,
                (status == null || status.isBlank()) ? "Active" : status
        );
    }

    private StudentRow toRow(Student s) {
        return new StudentRow(
                s.studentId(), s.fullName(), s.programme(), s.level(), s.gpa(),
                s.email(), s.phone(), s.status()
        );
    }

    private void clearForm() {
        idField.clear();
        nameField.clear();
        programmeCombo.getSelectionModel().clearSelection();
        programmeCombo.getEditor().clear();
        levelCombo.getSelectionModel().clearSelection();
        gpaField.clear();
        emailField.clear();
        phoneField.clear();
        statusCombo.getSelectionModel().clearSelection();
    }

    private void setStatus(String msg, boolean error) {
        if (statusLabel == null) return;
        statusLabel.setText(msg == null ? "" : msg);
        statusLabel.setStyle(error ? "-fx-text-fill: #ff4d4d;" : "-fx-text-fill: #9be15d;");
    }







}
