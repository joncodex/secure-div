package com.enzelascripts.securediv.repository;

import com.enzelascripts.securediv.entity.Transcript;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TranscriptRepo extends JpaRepository<Transcript, Long> {
    Optional<Transcript> getTranscriptByDocumentNumber(String documentNumber);

    boolean existsByStudent_IdAndDegreeAndCourse(Long studentId, String degree, String course);

    boolean existsByDocumentNumber(String documentNumber);
}
