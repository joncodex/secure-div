package com.enzelascripts.securediv.repository;

import com.enzelascripts.securediv.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentRepo extends JpaRepository<Document, Long> {
    boolean existsByDocumentNumber(String documentNumber);

    boolean existsByStudent_IdAndDegreeAndCourse(Long studentId, String degree, String course);

    Optional<Document> getDocumentByDocumentNumber(String documentNumber);
}
