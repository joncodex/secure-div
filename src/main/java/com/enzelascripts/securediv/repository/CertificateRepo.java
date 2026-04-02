package com.enzelascripts.securediv.repository;

import com.enzelascripts.securediv.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepo extends JpaRepository<Certificate, Long> {

    boolean existsByCertificateNumber(String certificateNumber);

    Optional<Certificate> findCertificateByCertificateNumber(String certificateNumber);

    Optional<Certificate> findCertificateById(Long id);

    boolean existsByStudent_IdAndDegreeAndCourse(Long studentId, String degree, String course);

    boolean existsByStudent_Id(Long studentId);

    boolean existsByStudent_IdAndCourseAndDegree(Long studentId, String course, String degree);
}
