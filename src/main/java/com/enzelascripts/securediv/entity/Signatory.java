package com.enzelascripts.securediv.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

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
    private String s3Key;

    private boolean current;        // current signatory
    private LocalDate createdAt;
    private LocalDate invalidatedAt;


 }

