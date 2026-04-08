package com.enzelascripts.securediv.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request object for submitting a student's course result")
public class CourseResultRequest {

    @NotBlank(message = "Student ID is required")
    @Schema(
            description = "Unique identifier of the student (e.g. matric number)",
            example = "MAT-2024-001"
    )
    private String studentId;

    @NotBlank(message = "Course Code is required")
    @Schema(
            description = "Course code",
            example = "CSC101"
    )
    private String courseCode;

    @NotBlank(message = "Course Title is required")
    @Schema(
            description = "Course title",
            example = "Introduction to Computer Science"
    )
    private String courseTitle;

    @Min(value = 1, message = "Credit Unit must be at least 1")
    @NotNull(message = "course unit cannot be null")
    @Schema(
            description = "Credit unit of the course",
            example = "3",
            minimum = "1"
    )
    private int courseUnit;

    @Min(value = 0, message = "Score must be at least 0")
    @Max(value = 100, message = "Score cannot be greater than 100")
    @NotNull(message = "Score cannot be null")
    @Schema(
            description = "Score obtained in the course (0–100)",
            example = "75",
            minimum = "0",
            maximum = "100"
    )
    private int score;

    @Min(value = 100, message = "100 Level is the first Level")
    @Schema(
            description = "Academic level of the student",
            example = "200",
            minimum = "100"
    )
    private int level;

    @NotBlank(message = "Semester cannot be empty")
    @Schema(
            description = "Semester of the course",
            example = "First"
    )
    private String semester; // First / Second

    @NotBlank(message = "Session cannot be empty")
    @Schema(
            description = "Academic session",
            example = "2023/2024"
    )
    private String session;  // e.g. 2022/2023
}