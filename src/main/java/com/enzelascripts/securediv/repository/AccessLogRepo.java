package com.enzelascripts.securediv.repository;

import com.enzelascripts.securediv.entity.AccessLog;
import com.enzelascripts.securediv.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccessLogRepo extends JpaRepository<AccessLog, Long> {

}
