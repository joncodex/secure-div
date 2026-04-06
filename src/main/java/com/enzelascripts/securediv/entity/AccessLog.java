package com.enzelascripts.securediv.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class AccessLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String documentId;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime accessedAt;
    private boolean downloadSuccessful;
}