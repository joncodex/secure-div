package com.enzelascripts.securediv.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
public class StudentResponse {
    private String studentId;       //or Matric Number
    private String firstName;
    private String lastName;
    private String gender;
    private String email;
    private String phoneNumber;

    private LocalDate dateOfBirth;

}
