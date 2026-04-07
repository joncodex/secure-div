package com.enzelascripts.securediv.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
@Schema(description = "Request object for creating or registering a student")
public class StudentRequest {
    @NotBlank(message = "Student ID cannot be blank")
    @Schema(
            description = "Unique student identifier (e.g. matric number)",
            example = "MAT-2024-001"
    )
    private String studentId;       //or Matric Number

    @NotBlank(message = "First Name cannot be blank")
    @Size(max = 50, message = "First Name must be 50 characters or less")
    @Schema(
            description = "Student's first name",
            example = "Chinedu"
    )
    private String firstName;

    @NotBlank(message = "Last Name cannot be blank")
    @Size(max = 50, message = "Last Name must be 50 characters or less")
    @Schema(
            description = "Student's last name",
            example = "Okafor"
    )
    private String lastName;

    @NotBlank(message = "Gender cannot be blank")
    @Size(max = 10, message = "Gender must be 10 characters or less")
    @Schema(
            description = "Gender of the student",
            example = "Male"
    )
    private String gender;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Please provide a valid email address")
    @Schema(
            description = "Student's email address",
            example = "chinedu.okafor@example.com"
    )
    private String email;

    @Schema(
            description = "Student's phone number (optional)",
            example = "+2348012345678"
    )
    private String phoneNumber;

    @NotNull(message = "Date of Birth cannot be blank")
    @Past(message = "Date of Birth must be in the past")
    @Schema(
            description = "Student's date of birth",
            example = "2000-05-15",
            type = "string",
            format = "date"
    )
    private LocalDate dateOfBirth;


}
