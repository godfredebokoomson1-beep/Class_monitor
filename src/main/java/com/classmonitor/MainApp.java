package com.classmonitor;

import com.classmonitor.repository.Db;
import com.classmonitor.ui.AppNavigator;
import com.classmonitor.util.AppLogger;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        Db.initSchema();
        AppLogger.init();
        AppLogger.log("APP_START");

        AppNavigator.init(stage);
        AppNavigator.goDashboard();
    }

    @Override
    public void stop() {
        AppLogger.log("APP_CLOSE");
    }

    public static void main(String[] args) {
        launch();
    }
}
