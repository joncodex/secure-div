package com.enzelascripts.securediv.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "documents", indexes = @Index(columnList = "documentNumber"))
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "doc_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(unique = true, nullable = false, length = 36)
    private String documentNumber;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private String degree;

    @Column(nullable = false)
    private String course;

    private String classOfDegree;
    private Double cgpaValue;
    private double cgpaScale;

    @Column(nullable = false)
    private LocalDate graduationDate;

    @Column(name = "doc_type", insertable = false, updatable = false)
    private String documentType;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private InstitutionRecord institutionRecord;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "document_signatories",
            joinColumns = @JoinColumn(name = "document_id"),
            inverseJoinColumns = @JoinColumn(name = "signatory_id")
    )
    private List<Signatory> signatories;

    @Column(nullable = false)
    private String s3Key;

    @Column(nullable = false, length = 64)
    private String sha256Hash;

    @Column(length = 64)
    private String metadataSignature;

    @Setter(AccessLevel.NONE)
    @Column(nullable = false, updatable = false)
    private LocalDateTime issuedAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private boolean revoked = false;

    @Column(columnDefinition = "TEXT")
    private String revocationReason;

    private LocalDateTime revokedAt;

    private String revokedBy;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.issuedAt = LocalDateTime.now();
        if (this.documentNumber == null) {
            this.documentNumber = java.util.UUID.randomUUID().toString();
        }
    }
}