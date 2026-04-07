package com.enzelascripts.securediv.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseResultRequest {

    @NotBlank(message = "Student ID is required")
    private String studentId;

    @NotBlank(message = "Course Code is required")
    private String courseCode;

    @NotBlank(message = "Course Title is required")
    private String courseTitle;

    @Min(value = 1, message = "Credit Unit must be at least 1")
    @NotNull(message = "course unit cannot be null")
    private int courseUnit;

    @Min(value = 0, message = "Score must be at least 0")
    @NotNull(message = "Score cannot be null")
    @Max(value = 100, message = "Score cannot be greater than 100")
    private int score;

    @Min(value = 100, message = "100 Level is the first Level")
    private int level;

    @NotBlank(message = "Semester cannot be empty")
    private String semester; // First / Second

    @NotBlank(message = "Session cannot be empty")
    private String session;  // e.g. 2022/2023
}