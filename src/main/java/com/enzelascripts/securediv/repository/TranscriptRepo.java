package com.enzelascripts.securediv.repository;

import com.enzelascripts.securediv.entity.Transcript;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TranscriptRepo extends JpaRepository<Transcript, Long> {
}
