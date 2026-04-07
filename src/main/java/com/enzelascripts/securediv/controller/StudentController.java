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

    // Create new student
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> create(
            @Valid
            @RequestBody
            StudentRequest dto) {

        StudentResponse response = service.createStudent(dto);

        Map<String, Object> responseMap = Map.of(
                "message", "Student created successfully",
                "data", response);

        return ResponseEntity.ok(responseMap);

    }

    // Get student
    @GetMapping("/{studentId}")
    public ResponseEntity<StudentResponse> get(@PathVariable String studentId) {

        return ResponseEntity.ok(service.getStudentAsResponse(studentId));
    }

    // Get all students
    @GetMapping("/all-student")
    public ResponseEntity<List<StudentResponse>> getAll() {

        List<StudentResponse> responseList = service.getAllStudents();
        return ResponseEntity.ok().body(responseList);
    }

    //update student
    @PutMapping("/update")
    public ResponseEntity<StudentResponse> update(
            @Valid
            @RequestBody
            StudentRequest dto) {

        StudentResponse response = service.updateStudent(dto);
        return ResponseEntity.ok().body(response);
    }

    //delete student
    @DeleteMapping("/delete/{studentId}")
    public ResponseEntity<Void> delete(@PathVariable String studentId) {

        service.deleteStudent(studentId);
        return ResponseEntity.ok().build() ;
    }

}


//http://localhost:8080/api/v1/students/create
//{
//        "studentId": "MAT-2024-001",
//        "firstName": "Chinedu",
//        "lastName": "Okafor",
//        "gender": "Male",
//        "email": "chinedu.okafor@example.com",
//        "phoneNumber": "+2348012345678",
//        "dateOfBirth": "2000-05-15"
//        }