package com.classmonitor.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.classmonitor.repository.sqlite.SqliteStudentRepository;
import com.classmonitor.service.StudentService;

import java.util.prefs.Preferences;


public final class AppNavigator {
    private static Stage stage;

    // Persist theme preference between app launches
    private static final Preferences PREFS = Preferences.userNodeForPackage(AppNavigator.class);
    private static final String PREF_DARK_MODE = "darkMode";
    private static boolean darkMode = PREFS.getBoolean(PREF_DARK_MODE, false);

    private AppNavigator(){}

    public static void init(Stage primaryStage) {
        stage = primaryStage;
        stage.setTitle("ClassMonitor - Student Management System Plus");
    }

    public static boolean isDarkMode() {
        return darkMode;
    }

    public static void setDarkMode(boolean enabled) {
        darkMode = enabled;
        PREFS.putBoolean(PREF_DARK_MODE, enabled);

        // apply to main window if available
        if (stage != null && stage.getScene() != null) {
            applyTheme(stage.getScene());
        }
    }

    /** Apply current theme (dark/light) to ANY scene (main or modal). */
    public static void applyTheme(Scene scene) {
        if (scene == null) return;

        // Remove any previous theme stylesheets we manage
        scene.getStylesheets().removeIf(s ->
                s.contains("/com/classmonitor/styles/light.css") ||
                        s.contains("/com/classmonitor/styles/dark.css") ||
                        s.contains("/com/classmonitor/styles/theme.css")
        );

        // Preferred: light.css / dark.css
        String preferred = darkMode
                ? "/com/classmonitor/styles/dark.css"
                : "/com/classmonitor/styles/light.css";

        var css = AppNavigator.class.getResource(preferred);

        // Fallback to existing theme.css if the new ones are not present
        if (css == null) {
            css = AppNavigator.class.getResource("/com/classmonitor/styles/theme.css");
        }

        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        } else {
            System.out.println("CSS NOT FOUND: styles/light.css, styles/dark.css, or styles/theme.css");
        }
    }

    public static void goDashboard() { setScene("/com/classmonitor/views/dashboard.fxml", 1000, 650); }
    public static void goStudents() { setScene("/com/classmonitor/views/students.fxml", 1200, 720); }
    public static void goReports() { setScene("/com/classmonitor/views/reports.fxml", 1100, 700); }
    public static void goImportExport() { setScene("/com/classmonitor/views/import_export.fxml", 1050, 680); }

    /**
     * Opens settings as a modal dialog so the user can save and return to the current screen.
     */
    public static void openSettingsModal() {
        try {
            FXMLLoader loader = new FXMLLoader(AppNavigator.class.getResource("/com/classmonitor/views/settings.fxml"));
            Scene scene = new Scene(loader.load(), 900, 650);
            applyTheme(scene);

            Stage dialog = new Stage();
            dialog.setTitle("Settings");
            dialog.initOwner(stage);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(scene);
            dialog.showAndWait();
        } catch (Exception e) {
            throw new RuntimeException("Failed to open Settings window: " + e.getMessage(), e);
        }
    }

    // Convenience overload used across controllers
    public static void setScene(String fxml) {
        setScene(fxml, 1200, 720);
    }

    public static void setScene(String fxml, int w, int h) {
        try {
            FXMLLoader loader = new FXMLLoader(AppNavigator.class.getResource(fxml));
            Scene scene = new Scene(loader.load(), w, h);
            applyTheme(scene);

            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load UI: " + fxml + " -> " + e.getMessage(), e);
        }
    }

    private static final SqliteStudentRepository STUDENT_REPO =
            new SqliteStudentRepository();

    private static final StudentService STUDENT_SERVICE =
            new StudentService(STUDENT_REPO);

    public static StudentService studentService() {
        return STUDENT_SERVICE;
    }


}
