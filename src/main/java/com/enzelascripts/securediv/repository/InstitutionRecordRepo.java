package com.enzelascripts.securediv.repository;

import com.enzelascripts.securediv.entity.InstitutionRecord;
import com.enzelascripts.securediv.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstitutionRecordRepo extends JpaRepository<InstitutionRecord, Long> {

    Optional<InstitutionRecord> findInstitutionRecordByCurrent(boolean current);

    List<InstitutionRecord> getInstitutionRecordsByCurrent(boolean current);
}
