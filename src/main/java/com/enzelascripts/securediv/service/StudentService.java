package com.enzelascripts.securediv.service;

import com.enzelascripts.securediv.entity.CourseResult;
import com.enzelascripts.securediv.entity.Student;
import com.enzelascripts.securediv.exception.ResourceNotFoundException;
import com.enzelascripts.securediv.repository.CourseResultRepo;
import com.enzelascripts.securediv.repository.StudentRepo;
import com.enzelascripts.securediv.request.StudentRequest;
import com.enzelascripts.securediv.response.StudentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.enzelascripts.securediv.util.Utility.transferData;
import static com.enzelascripts.securediv.util.Utility.validateNotNull;

@Service
public class StudentService {

//  ============================================== Fields ==================================================
    @Autowired
    private StudentRepo studentRepo;
    @Autowired
    private CourseResultRepo courseResultRepo;


    //  ===========================================public methods ===============================================
    public StudentResponse createStudent(StudentRequest dto) {

        // null check and transfer data
        validateNotNull(dto);
        Student student = transferData(dto, new Student());

        //save the student
        studentRepo.save(student);

        //return the student response object
        return transferData(student, new StudentResponse());

    }

    public StudentResponse updateStudent(StudentRequest dto) {

        //validate method argument
        validateNotNull(dto);

        //get the student object
        Student existingStudent = studentRepo
                .findStudentByStudentId(dto.getStudentId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("the student with ID: " + dto.getStudentId() + " was not found"));

        return transferData(existingStudent, new StudentResponse());
    }

    public List<StudentResponse> getAllStudents() {

        return studentRepo.findAll()
                .stream()
                .map(s ->
                        transferData(s, new StudentResponse()))
                .toList();
    }

    public StudentResponse getStudentAsResponse(String studentId) {

        Student student = getStudentByStudentId(studentId);
        return transferData(student, new StudentResponse());
    }

    public Student getStudentByStudentId(String studentId) {

        return studentRepo.findStudentByStudentId(studentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "the student with ID: " + studentId + " was not found"));
    }

    @Transactional
    public void deleteStudent(String studentId) {

        //Get student's course results
        Student student = getStudentByStudentId(studentId);
        List<CourseResult> courseResultList = courseResultRepo
                .getCourseResultsByStudent_Id(student.getId());

        //delete them, to avoid orphaned course results
        courseResultRepo.deleteAll(courseResultList);

        //delete student
        studentRepo.deleteByStudentId(studentId);
    }


}