package com.enzelascripts.securediv.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Signatory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String position;
    @Column(columnDefinition = "TEXT")
    private String signatureUrl;

    private boolean current;        // current signatory
    private LocalDate createdAt;
    private LocalDate invalidatedAt;
 }

