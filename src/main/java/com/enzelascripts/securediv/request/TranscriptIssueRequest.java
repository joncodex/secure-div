package com.enzelascripts.securediv.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptIssueRequest extends DocumentIssueRequest {
    
    @NotEmpty(message = "Course results are required for transcript")
    @Valid
    private List<CourseResultRequest> courseResults;
    
    private String programName;     // e.g., "B.Sc. Geology Programme"
    private Integer totalSemesters;
}