package com.enzelascripts.securediv.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Certificate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String certificateNumber;

    @OneToOne
    @JoinColumn(name = "student_id")
    private Student student;

    private String degree;          // Bachelor of Science
    private String course;          // Geology
    private String classOfDegree;   // First-Class Honors
    @Column(columnDefinition = "TEXT")
    private String qrCode;
    @Column(columnDefinition = "TEXT")
    private String downloadUrl;     // On the object storage

    private LocalDate graduationDate;

    @ManyToOne
    @JoinColumn(name = "institution_record_id")
    private InstitutionRecord institutionRecord;

    @ManyToMany
    @JoinTable(name = "certificate_signatory_id")
    private List<Signatory> signatories;

    private boolean isValid;

    @Column(columnDefinition = "TEXT")
    private String fingerprint;
    private String s3Key;


}