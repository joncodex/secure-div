package com.enzelascripts.securediv.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(indexes = @Index(columnList = "documentNumber"))
public class AccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 36)
    private String documentNumber;  // code generated

    @Column(nullable = false, length = 20)
    private String action;          // "VERIFY", "DOWNLOAD", "ISSUED"

    private String ipAddress;
    private String userAgent;
    private String principalUser;  // Who accessed (for tracking)

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private boolean successful = true;

    @Column(columnDefinition = "TEXT")
    private String notes;           // Error messages, etc.

}