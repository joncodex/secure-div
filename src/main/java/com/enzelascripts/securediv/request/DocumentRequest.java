package com.enzelascripts.securediv.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public abstract class DocumentRequest {

    @NotBlank(message= "student ID is required")
    @Size(max = 25, message = "Cannot be more than 25 characters")
    private String studentId;

    @NotBlank(message = "degree is required")
    private String degree;          // Bachelor of Science

    @NotBlank(message = "Course studied is required")
    private String course;          // Geology

    @NotBlank(message = "class of Degree this student graduated with is required")
    private String classOfDegree;   // First-class Honors

    @NotNull(message = "Please provide the graduation date")
    @Past(message = "Graduation date must be in the past")
    private LocalDate graduationDate;
}
