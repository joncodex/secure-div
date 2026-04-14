package com.enzelascripts.securediv.repository;

import com.enzelascripts.securediv.entity.Certificate;
import com.enzelascripts.securediv.entity.Document;
import com.enzelascripts.securediv.entity.Transcript;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CertificateRepo extends JpaRepository<Certificate, Long> {
    boolean existsByDocumentNumber(String documentNumber);

    boolean existsByStudent_IdAndDegreeAndCourse(Long studentId, String degree, String course);

    Optional<Certificate> getCertificateByDocumentNumber(String documentNumber);

}
