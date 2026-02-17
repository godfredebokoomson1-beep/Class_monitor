package classmonitor;

import com.classmonitor.domain.Student;
import com.classmonitor.repository.Db;
import com.classmonitor.repository.sqlite.SqliteStudentRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RepositorySmokeTest {

    @Test
    void canInsertAndRead() {
        Db.initSchema();
        SqliteStudentRepository repo = new SqliteStudentRepository();

        String id = "TEST" + System.currentTimeMillis();

        Student s = new Student(id, "Test User", "IT", 100, 3.0,
                "t@t.com", "0123456789", "2026-02-02", "Active");

        repo.add(s);

        var got = repo.findById(id);
        Assertions.assertTrue(got.isPresent());
        Assertions.assertEquals(id, got.get().studentId());
    }
}
