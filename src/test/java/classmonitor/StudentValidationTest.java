package classmonitor;

import com.classmonitor.domain.Student;
import com.classmonitor.repository.StudentRepository;
import com.classmonitor.service.StudentService;
import com.classmonitor.ValidationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

class StudentValidationTest {

    // In-memory repo for validation tests
    static class FakeRepo implements StudentRepository {
        Set<String> ids = new HashSet<>();
        @Override public boolean existsById(String studentId){ return ids.contains(studentId); }
        @Override public void add(Student s){ ids.add(s.studentId()); }
        @Override public void update(Student s){}
        @Override public void delete(String studentId){}
        @Override public Optional<Student> findById(String studentId){ return Optional.empty(); }
        @Override public List<Student> findAll(){ return List.of(); }
        @Override public List<Student> search(String query){ return List.of(); }
    }

    private Student good() {
        return new Student("UMAT1234","Hash Mensah","Computer Science",200,3.5,
                "hash@example.com","0241234567","2026-02-02","Active");
    }

    @Test
    void rejectsDuplicateId() {
        FakeRepo repo = new FakeRepo();
        repo.ids.add("UMAT1234");
        StudentService svc = new StudentService(repo);

        Assertions.assertThrows(ValidationException.class, () -> svc.add(good()));
    }

    @Test
    void rejectsBadGpa() {
        FakeRepo repo = new FakeRepo();
        StudentService svc = new StudentService(repo);

        Student s = new Student("UMAT1234","Hash Mensah","Computer Science",200,4.7,
                "hash@example.com","0241234567","2026-02-02","Active");

        Assertions.assertThrows(ValidationException.class, () -> svc.add(s));
    }

    @Test
    void acceptsGoodStudent() {
        FakeRepo repo = new FakeRepo();
        StudentService svc = new StudentService(repo);

        Assertions.assertDoesNotThrow(() -> svc.add(good()));
    }
}
