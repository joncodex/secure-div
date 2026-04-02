package com.enzelascripts.securediv.repository;

import com.enzelascripts.securediv.entity.Signatory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SignatoryRepo extends JpaRepository<Signatory, Long> {
    List<Signatory> findSignatoriesByCurrent(boolean active);


    Optional<Signatory> findSignatoryById(Long id);

    Optional<Signatory> findSignatoryByName(String name);
}
