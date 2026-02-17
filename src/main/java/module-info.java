module classmonitor {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.slf4j;
    requires java.prefs;
    requires java.desktop;

    opens com.classmonitor.ui.controllers to javafx.fxml;
    exports com.classmonitor;
}
