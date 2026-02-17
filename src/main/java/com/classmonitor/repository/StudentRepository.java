package com.classmonitor.repository;

import com.classmonitor.domain.Student;

import java.util.List;
import java.util.Optional;

public interface StudentRepository {
    boolean existsById(String studentId);
    void add(Student s);
    void update(Student s);
    void delete(String studentId);
    Optional<Student> findById(String studentId);
    List<Student> findAll();
    List<Student> search(String query); // by id or name
}
