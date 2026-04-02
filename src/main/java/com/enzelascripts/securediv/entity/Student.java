package com.enzelascripts.securediv.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Setter
@Getter
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String studentId;       //or Matric Number
    private String firstName;
    private String lastName;
    private String gender;
    private String email;
    private String phoneNumber;

    private LocalDate dateOfBirth;

    @JsonBackReference
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    private List<CourseResult> courseResults;


}
