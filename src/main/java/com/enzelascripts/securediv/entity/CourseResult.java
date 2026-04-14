package com.enzelascripts.securediv.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class CourseResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    private String courseCode;
    private String courseTitle;

    private int courseUnit;
    private int score;

    private int level;

    private String semester; // First / Second
    private String session;  // e.g. 2022/2023

}