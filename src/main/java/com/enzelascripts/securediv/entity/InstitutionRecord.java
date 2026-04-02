package com.enzelascripts.securediv.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class InstitutionRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Institution details
    private String institutionName;
    private String address;
    @Column(columnDefinition = "TEXT")
    private String logoUrl;     // URL to logo image
    private String motto;
    private String email;
    private String phoneNumber;
    private String website;
    private LocalDate createdAt;
    private LocalDate invalidatedAt;

    private boolean current;     // current institution record

 }

