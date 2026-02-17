package com.classmonitor.ui.models;

import javafx.beans.property.*;

public class StudentRow {
    private final StringProperty studentId = new SimpleStringProperty();
    private final StringProperty fullName = new SimpleStringProperty();
    private final StringProperty programme = new SimpleStringProperty();
    private final IntegerProperty level = new SimpleIntegerProperty();
    private final DoubleProperty gpa = new SimpleDoubleProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty phone = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();

    public StudentRow(String studentId, String fullName, String programme, int level, double gpa,
                      String email, String phone, String status) {
        this.studentId.set(studentId);
        this.fullName.set(fullName);
        this.programme.set(programme);
        this.level.set(level);
        this.gpa.set(gpa);
        this.email.set(email);
        this.phone.set(phone);
        this.status.set(status);
    }

    public String getStudentId() { return studentId.get(); }
    public StringProperty studentIdProperty() { return studentId; }

    public String getFullName() { return fullName.get(); }
    public StringProperty fullNameProperty() { return fullName; }

    public String getProgramme() { return programme.get(); }
    public StringProperty programmeProperty() { return programme; }

    public int getLevel() { return level.get(); }
    public IntegerProperty levelProperty() { return level; }

    public double getGpa() { return gpa.get(); }
    public DoubleProperty gpaProperty() { return gpa; }

    public String getEmail() { return email.get(); }
    public StringProperty emailProperty() { return email; }

    public String getPhone() { return phone.get(); }
    public StringProperty phoneProperty() { return phone; }

    public String getStatus() { return status.get(); }
    public StringProperty statusProperty() { return status; }
}
