package com.enzelascripts.securediv.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
public class StudentRequest {
    @NotBlank(message = "Student ID cannot be blank")
    private String studentId;       //or Matric Number

    @NotBlank(message = "First Name cannot be blank")
    @Size(max = 50, message = "First Name must be 50 characters or less")
    private String firstName;

    @NotBlank(message = "Last Name cannot be blank")
    @Size(max = 50, message = "Last Name must be 50 characters or less")
    private String lastName;

    @NotBlank(message = "Gender cannot be blank")
    @Size(max = 10, message = "Gender must be 10 characters or less")
    private String gender;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Please provide a valid email address")
    private String email;

    private String phoneNumber;

    @NotNull(message = "Date of Birth cannot be blank")
    @Past(message = "Date of Birth must be in the past")
    private LocalDate dateOfBirth;

    List<String> courseCodes;


}
