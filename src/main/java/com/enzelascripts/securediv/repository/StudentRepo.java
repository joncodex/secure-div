package com.enzelascripts.securediv.repository;

import com.enzelascripts.securediv.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepo extends JpaRepository<Student, Long> {
    Optional<Student> findStudentByStudentId(String studentId);

    Optional<Student> findStudentById(Long id);

    void deleteByStudentId(String studentId);
}
