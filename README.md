CLASS MONITOR (Student Management System)

A JavaFX + SQLite student management system built for the Mid-Sem Capstone (OOP Using Java).
It supports student CRUD, validation, CSV import/export with error reporting, and dashboard analytics.

 Features (Functional Requirements)
  Student Management
- Add a new student
- View students in a table
- Update student details
- Delete student (with confirmation)
- Search by Student ID or Full Name
- Filter by Programme, Level, Status
- Sort by GPA / Name

 Reports & Analytics
- Top performers (Top 10 by GPA) with filters
- At-risk students (below GPA threshold set in Settings)
- GPA distribution summary (dashboard chart)
- Programme summary (counts + average GPA per programme)

Import / Export (CSV)
- Export all students to CSV
- Export top performers to CSV
- Export at-risk students to CSV
- Import students from CSV with validation:
  - invalid rows are skipped (app does not crash)
  - invalid rows are logged in an import error report
  - duplicate Student IDs are rejected and reported

> All exports/import logs are saved in the project-controlled `data/` folder.

Validation Rules (Service + UI)
- Student ID: required, unique, letters/digits only
- Full name: required, no digits
- Programme: required
- Level: one of 100–700
- GPA: 0.0 to 4.0
- Email: must contain `@` and `.`
- Phone: 10–15 digits, digits only

Tech Stack
- Java (JDK used in development: see RUN_VM_OPTIONS.txt)
- JavaFX (UI)
- SQLite + JDBC (database)
- Maven (build & tests)

Project Structure (Typical)
- `src/main/java` - Java source (UI controllers, services, repositories, models)
- `src/main/resources` - FXML views and app resources
- `data/` - database file, exports, import error reports, logs
- `evidence/` - screenshots + `mvn test` output text (required deliverable)

How to Run (Windows)
1. Install JDK (same version used in development is stated in `RUN_VM_OPTIONS.txt`)
2. Ensure JavaFX SDK is available on your machine
3. Open the project in IntelliJ (or any IDE)
4. Set VM Options (copy from `RUN_VM_OPTIONS.txt`)
5. Run the main class (e.g., `com.classmonitor.MainApp`)

 How to Run Tests
```bash
mvn test
