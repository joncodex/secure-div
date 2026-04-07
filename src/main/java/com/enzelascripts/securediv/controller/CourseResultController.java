package com.enzelascripts.securediv.controller;

import com.enzelascripts.securediv.request.CourseResultRequest;
import com.enzelascripts.securediv.response.CourseResultResponse;
import com.enzelascripts.securediv.service.CourseResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/course-results")
@RequiredArgsConstructor
@Tag(name = "Course Results", description = "APIs for managing student course results")
public class CourseResultController {

    private final CourseResultService service;

    @Operation(summary = "Create a new course result")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Course result created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping("/create")
    public ResponseEntity<CourseResultResponse> create(
            @Valid @RequestBody CourseResultRequest dto) {

        CourseResultResponse response = service.createCourseResult(dto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get a course result by course code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Course result retrieved"),
            @ApiResponse(responseCode = "404", description = "Course not found")
    })
    @GetMapping("{studentId}/{courseCode}")
    public ResponseEntity<CourseResultResponse> get(@PathVariable String studentId, @PathVariable  String courseCode) {

        CourseResultResponse response =
                service.getCourseResultResponseByCourseCode(studentId, courseCode);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update a course result")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Course updated successfully"),
            @ApiResponse(responseCode = "404", description = "Course not found")
    })
    @PutMapping("/update")
    public ResponseEntity<CourseResultResponse> update(
            @Valid @RequestBody CourseResultRequest dto) {

        CourseResultResponse response = service.update(dto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete a course result")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Course deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Course not found")
    })
    @DeleteMapping("/{studentId}/{courseCode}")
    public ResponseEntity<Void> delete(@PathVariable String studentId, @PathVariable String courseCode) {

        service.delete(studentId, courseCode);
        return ResponseEntity.ok().build();
    }
}


//PUT http://localhost:8080/api/v1/course-results/CSC101/update
//Content-Type: application/json

//{
//        "studentId": "MAT-2024-001",
//        "courseCode": "CSC101",
//        "courseTitle": "Intro to Computer Science",
//        "courseUnit": 3,
//        "score": 85,
//        "level": 200,
//        "semester": "First",
//        "session": "2023/2024"
//        }