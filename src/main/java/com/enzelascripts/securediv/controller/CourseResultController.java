package com.enzelascripts.securediv.controller;

import com.enzelascripts.securediv.request.CourseResultRequest;
import com.enzelascripts.securediv.response.CourseResultResponse;
import com.enzelascripts.securediv.service.CourseResultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/course-results")
@RequiredArgsConstructor
public class CourseResultController {

    private final CourseResultService service;

    // Create a new course result
    @PostMapping("/create")
    public ResponseEntity<CourseResultResponse> create(
            @Valid
            @RequestBody
            CourseResultRequest dto) {

        CourseResultResponse response = service.createCourseResult(dto);
        return ResponseEntity.ok(response);

    }

    // Get a course
    @GetMapping("/{courseCode}")
    public ResponseEntity<CourseResultResponse> get(@PathVariable String courseCode) {

        CourseResultResponse response = service.getCourseResultByCourseCode(courseCode);
        return ResponseEntity.ok(response);
    }

    //update a course
    @PutMapping("/update-courses/{courseCode}")
    public ResponseEntity<CourseResultResponse> update(
            @PathVariable String courseCode,
            @RequestBody CourseResultRequest dto) {

        CourseResultResponse response = service.update (courseCode, dto);
        return ResponseEntity.ok().body(response);
    }

    //delete course result
    @DeleteMapping("/delete/{courseCode}")
    public ResponseEntity<Void> delete(@PathVariable String courseCode) {

        service.delete(courseCode);
        return ResponseEntity.ok().build();
    }

}