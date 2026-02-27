package com.classmonitor.ui.controllers;

import com.classmonitor.repository.Db;
import com.classmonitor.repository.SettingsDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Locale;

public class SettingsController {

    // ===== Thresholds =====
    @FXML private Spinner<Double> spAtRisk;
    @FXML private Spinner<Double> spAverage;
    @FXML private Spinner<Double> spTop;
    @FXML private Label lblThresholdHint;

    // ===== Programmes =====
    @FXML private ListView<String> lvProgrammes;
    @FXML private TextField txtProgramme;
    @FXML private Label lblProgMsg;

    private SettingsDAO settingsDAO;
    private Connection conn;

    // Optional callback (Dashboard can hook this)
    public static Runnable onSettingsChanged = null;

    @FXML
    public void initialize() {
        try {
            conn = Db.get();                 // ✅ your Db.java uses Db.get()
            settingsDAO = new SettingsDAO(conn);

            setupSpinners();
            updateHint();

            ensureProgrammesTable();
            loadProgrammes();

            lvProgrammes.getSelectionModel().selectedItemProperty().addListener((obs, o, v) -> {
                if (v != null) txtProgramme.setText(v);
            });

        } catch (Exception e) {
            showError("Failed to load settings", e.getMessage());
        }
    }

    // ==============================
    // GPA THRESHOLDS
    // ==============================

    private void setupSpinners() {
        double atRisk = settingsDAO.getAtRiskThreshold();
        double avg    = settingsDAO.getAverageThreshold();
        double top    = settingsDAO.getTopThreshold();

        spAtRisk.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.00, 5.00, atRisk, 0.10));
        spAverage.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.00, 5.00, avg, 0.10));
        spTop.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.00, 5.00, top, 0.10));

        spAtRisk.setEditable(true);
        spAverage.setEditable(true);
        spTop.setEditable(true);

        spAtRisk.getEditor().textProperty().addListener((o, a, b) -> updateHint());
        spAverage.getEditor().textProperty().addListener((o, a, b) -> updateHint());
        spTop.getEditor().textProperty().addListener((o, a, b) -> updateHint());
    }

    private void updateHint() {
        try {
            double ar = parseSpinner(spAtRisk);
            double av = parseSpinner(spAverage);
            double tp = parseSpinner(spTop);

            lblThresholdHint.setText(String.format(Locale.US,
                    "At-Risk < %.2f | Average: %.2f – %.2f | Good: %.2f – %.2f | Top ≥ %.2f",
                    ar, ar, av, av, tp, tp
            ));
        } catch (Exception e) {
            lblThresholdHint.setText("Invalid input. Example: 2.50");
        }
    }

    @FXML
    private void onSave() {
        try {
            double ar = parseSpinner(spAtRisk);
            double av = parseSpinner(spAverage);
            double tp = parseSpinner(spTop);

            if (!(ar < av && av < tp)) {
                throw new IllegalArgumentException("Thresholds must follow: At-Risk < Average < Top");
            }

            settingsDAO.setAtRiskThreshold(round2(ar));
            settingsDAO.setAverageThreshold(round2(av));
            settingsDAO.setTopThreshold(round2(tp));

            if (onSettingsChanged != null) onSettingsChanged.run();

            Alert a = new Alert(Alert.AlertType.INFORMATION, "Settings saved.", ButtonType.OK);
            a.setHeaderText(null);
            a.showAndWait();

        } catch (Exception e) {
            showError("Could not save settings", e.getMessage());
        }
    }

    private double parseSpinner(Spinner<Double> sp) {
        String txt = sp.getEditor().getText();
        double v = Double.parseDouble(txt);
        if (v < 0 || v > 5) throw new IllegalArgumentException("Value must be between 0.00 and 5.00");
        return v;
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    // ==============================
    // PROGRAMME MANAGEMENT (SQLite)
    // ==============================

    private void ensureProgrammesTable() throws Exception {
        String sql = """
            CREATE TABLE IF NOT EXISTS programmes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE
            )
            """;
        try (var st = conn.createStatement()) {
            st.execute(sql);
        }
    }

    private void loadProgrammes() {
        try {
            ArrayList<String> list = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement("SELECT name FROM programmes ORDER BY name");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(rs.getString(1));
            }
            lvProgrammes.setItems(FXCollections.observableArrayList(list));
            lblProgMsg.setText("");
        } catch (Exception e) {
            showError("Failed to load programmes", e.getMessage());
        }
    }

    @FXML
    private void onAddProgramme() {
        String name = safeProgrammeName();
        if (name == null) return;

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT OR IGNORE INTO programmes(name) VALUES(?)"
        )) {
            ps.setString(1, name);
            int changed = ps.executeUpdate();

            if (changed == 0) {
                lblProgMsg.setText("Programme already exists: " + name);
            } else {
                lblProgMsg.setText("Added: " + name);
            }

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

        String newName = safeProgrammeName();
        if (newName == null) return;

        // No change
        if (selected.equalsIgnoreCase(newName)) {
            lblProgMsg.setText("No changes made.");
            return;
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE programmes SET name = ? WHERE name = ?"
        )) {
            ps.setString(1, newName);
            ps.setString(2, selected);
            int updated = ps.executeUpdate();

            if (updated == 0) {
                lblProgMsg.setText("Rename failed (programme not found).");
            } else {
                lblProgMsg.setText("Renamed to: " + newName);
            }

            loadProgrammes();
            lvProgrammes.getSelectionModel().select(newName);

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

        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Delete programme: " + selected + " ?",
                ButtonType.CANCEL, ButtonType.OK
        );
        confirm.setHeaderText(null);

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM programmes WHERE name = ?"
        )) {
            ps.setString(1, selected);
            ps.executeUpdate();

            lblProgMsg.setText("Deleted: " + selected);
            loadProgrammes();
            txtProgramme.clear();

        } catch (Exception e) {
            showError("Could not delete programme", e.getMessage());
        }
    }

    private String safeProgrammeName() {
        String name = (txtProgramme.getText() == null) ? "" : txtProgramme.getText().trim();
        if (name.isBlank()) {
            lblProgMsg.setText("Programme name cannot be empty.");
            return null;
        }
        return name;
    }

    // ==============================
    // Close + Alerts
    // ==============================

    @FXML
    private void onClose() {
        Stage st = (Stage) spAtRisk.getScene().getWindow();
        st.close();
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText(title);
        a.showAndWait();
    }
}
