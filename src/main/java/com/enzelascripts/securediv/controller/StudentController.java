package com.enzelascripts.securediv.controller;

import com.enzelascripts.securediv.request.StudentRequest;
import com.enzelascripts.securediv.response.CourseResultResponse;
import com.enzelascripts.securediv.response.StudentResponse;
import com.enzelascripts.securediv.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService service;

    // Create new signatory
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> create(
            @Valid
            @RequestBody
            StudentRequest dto) {

        StudentResponse response = service.createStudent(dto);

        String message = "Student created successfully";
        String courseResultInfo = service.infoAboutCourseCodesProvided(
                response.getCourseResultsResponse(), dto.getCourseCodes());

        Map<String, Object> responseMap = Map.of(
                "message", message + "\n" + courseResultInfo,
                "data", response
        );

        return ResponseEntity.ok(responseMap);

    }

    // Get student
    @GetMapping("/{studentId}")
    public ResponseEntity<StudentResponse> get(@PathVariable String studentId) {

        StudentResponse response = service.getStudentAsResponse(studentId);
        return ResponseEntity.ok(response);
    }

    // Get all students
    @GetMapping("/all-student")
    public ResponseEntity<List<StudentResponse>> getAll() {

        List<StudentResponse> response = service.getAllStudents();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/course-results/{studentId}")
    public ResponseEntity<List<CourseResultResponse>> getCourseResults(
            @PathVariable String studentId) {

        return ResponseEntity.ok(service.getStudentCourseResults(studentId));
    }

    //update student courses
    @PutMapping("/update-courses/{studentId}")
    public ResponseEntity<StudentResponse> update(
            @PathVariable String studentId,
            @RequestBody List<String> courseCodes) {

        StudentResponse response = service.updateStudentCourseResults(studentId, courseCodes);
        return ResponseEntity.ok().body(response);
    }

    //update student
    @PutMapping("/update/{studentId}")
    public ResponseEntity<StudentResponse> update(
            @PathVariable String studentId,
            @Valid
            @RequestBody
            StudentRequest dto) {

        StudentResponse response = service.updateStudent(dto, studentId);
        return ResponseEntity.ok().body(response);
    }

    //delete student
    @DeleteMapping("/delete/{studentId}")
    public ResponseEntity<Void> delete(@PathVariable String studentId) {

        service.deleteStudent(studentId);
        return ResponseEntity.ok().build();
    }

}