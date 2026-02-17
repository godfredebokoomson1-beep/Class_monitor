package com.classmonitor.ui.controllers;

import com.classmonitor.repository.ProgrammeDAO;
import com.classmonitor.repository.SettingsDAO;
import com.classmonitor.repository.Db;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;

public class SettingsController {

    @FXML private Spinner<Double> spThreshold;
    @FXML private Label lblThresholdHint;

    @FXML private ListView<String> lvProgrammes;
    @FXML private TextField txtProgramme;
    @FXML private Label lblProgMsg;

    private SettingsDAO settingsDAO;
    private ProgrammeDAO programmeDAO;

    // Event hook so other screens can refresh after settings change
    public static Runnable onSettingsChanged = null;

    @FXML
    public void initialize() {
        try {
            Connection conn = Db.get();
            settingsDAO = new SettingsDAO(conn);
            programmeDAO = new ProgrammeDAO(conn);

            setupThresholdSpinner();
            loadProgrammes();

            lvProgrammes.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
                if (newV != null) txtProgramme.setText(newV);
            });

        } catch (Exception e) {
            showError("Failed to load settings", e.getMessage());
        }
    }

    private void setupThresholdSpinner() {
        double current = settingsDAO.getAtRiskThreshold();

        SpinnerValueFactory.DoubleSpinnerValueFactory vf =
                new SpinnerValueFactory.DoubleSpinnerValueFactory(0.00, 5.00, current, 0.10);

        spThreshold.setValueFactory(vf);
        spThreshold.setEditable(true);

        lblThresholdHint.setText("Current threshold: " + String.format(java.util.Locale.US, "%.2f", current));

        // Validate user typed values
        spThreshold.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            try {
                double v = Double.parseDouble(newText);
                if (v < 0 || v > 5) {
                    lblThresholdHint.setText("Enter a value between 0.00 and 5.00");
                } else {
                    lblThresholdHint.setText("Students with GPA < " + String.format(java.util.Locale.US, "%.2f", v) + " are AT RISK");
                }
            } catch (Exception ex) {
                lblThresholdHint.setText("Invalid number format (e.g., 2.50)");
            }
        });
    }

    private void loadProgrammes() {
        try {
            lvProgrammes.setItems(FXCollections.observableArrayList(programmeDAO.getAllProgrammes()));
            lblProgMsg.setText("");
        } catch (Exception e) {
            showError("Failed to load programmes", e.getMessage());
        }
    }

    @FXML
    private void onSave() {
        try {
            double v = parseThreshold();
            settingsDAO.setAtRiskThreshold(v);
            lblThresholdHint.setText("Saved. Students with GPA < " + String.format(java.util.Locale.US, "%.2f", v) + " are AT RISK");

            if (onSettingsChanged != null) onSettingsChanged.run();

            Alert a = new Alert(Alert.AlertType.INFORMATION, "Settings saved successfully.", ButtonType.OK);
            a.setHeaderText(null);
            a.showAndWait();

        } catch (Exception e) {
            showError("Could not save settings", e.getMessage());
        }
    }

    private double parseThreshold() {
        String txt = spThreshold.getEditor().getText();
        double v = Double.parseDouble(txt);
        if (v < 0 || v > 5) throw new IllegalArgumentException("Threshold must be between 0.00 and 5.00");
        return Math.round(v * 100.0) / 100.0;
    }

    @FXML
    private void onClose() {
        Stage st = (Stage) spThreshold.getScene().getWindow();
        st.close();
    }

    // Programme actions
    @FXML
    private void onAddProgramme() {
        String name = safeName();
        if (name == null) return;

        try {
            programmeDAO.addProgramme(name);
            lblProgMsg.setText("Added: " + name);
            loadProgrammes();
            txtProgramme.clear();
        } catch (Exception e) {
            showError("Could not add programme", e.getMessage());
        }
    }

    @FXML
    private void onRenameProgramme() {
        String selected = lvProgrammes.getSelectionModel().getSelectedItem();
        if (selected == null) {
            lblProgMsg.setText("Select a programme to rename.");
            return;
        }

        String newName = safeName();
        if (newName == null) return;

        try {
            programmeDAO.renameProgramme(selected, newName);
            lblProgMsg.setText("Renamed to: " + newName);
            loadProgrammes();
        } catch (Exception e) {
            showError("Could not rename programme", e.getMessage());
        }
    }

    @FXML
    private void onDeleteProgramme() {
        String selected = lvProgrammes.getSelectionModel().getSelectedItem();
        if (selected == null) {
            lblProgMsg.setText("Select a programme to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete programme: " + selected + " ?", ButtonType.CANCEL, ButtonType.OK);
        confirm.setHeaderText(null);

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try {
            programmeDAO.deleteProgramme(selected);
            lblProgMsg.setText("Deleted: " + selected);
            loadProgrammes();
            txtProgramme.clear();
        } catch (Exception e) {
            showError("Could not delete programme", e.getMessage());
        }
    }

    private String safeName() {
        String name = txtProgramme.getText() == null ? "" : txtProgramme.getText().trim();
        if (name.isBlank()) {
            lblProgMsg.setText("Programme name cannot be empty.");
            return null;
        }
        return name;
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText(title);
        a.showAndWait();
    }
}
