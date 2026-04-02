package com.enzelascripts.securediv.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class Transcript {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Student student;
    private String institutionName;

    @OneToMany
    @JoinColumn(name = "transcript_id")
    private List<CourseResult> results;

    private double cgpa;
    private String degreeClass; // e.g., First Class, Second Class Upper

    // Constructors, Getters, Setters
}