package com.enzelascripts.securediv.service;

import com.enzelascripts.securediv.entity.CourseResult;
import com.enzelascripts.securediv.entity.Student;
import com.enzelascripts.securediv.exception.ResourceNotFoundException;
import com.enzelascripts.securediv.repository.StudentRepo;
import com.enzelascripts.securediv.request.StudentRequest;
import com.enzelascripts.securediv.response.CourseResultResponse;
import com.enzelascripts.securediv.response.StudentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.enzelascripts.securediv.util.Utility.transferData;
import static com.enzelascripts.securediv.util.Utility.validateNotNull;

@Service
public class StudentService {

    ///  ============================================== Fields ==================================================
    @Autowired
    private StudentRepo studentRepo;
    @Autowired
    private CourseResultService courseResultService;


    ///  ===========================================public methods ===============================================

    public StudentResponse createStudent(StudentRequest dto) {

        // transfer the data from request to student object
        validateNotNull(dto);
        Student student = transferData(dto, new Student());
        List<CourseResult> courseResults = new ArrayList<>();
        dto.getCourseCodes().forEach(courseCode -> courseResults
                .add(courseResultService.findCourseResultByCourseCode(courseCode)));

        student.setCourseResults(courseResults);
        return processStudentRequest(dto, student);

    }

    public String infoAboutCourseCodesProvided(List<CourseResultResponse> courseResultsResponse, List<String> courseCodes) {

        return courseResultService.compareCourseResultAndCourseCodes(courseResultsResponse, courseCodes);
    }

    public StudentResponse updateStudent(StudentRequest dto, String studentId) {

        //validate method arguments
        validateNotNull(studentId);
        validateNotNull(dto);

        //get the student object
        Student existingStudent = studentRepo.findStudentByStudentId(studentId).orElseThrow(() ->
                new ResourceNotFoundException("the student with ID: " + studentId + " was not found"));

        return processStudentRequest(dto, existingStudent);
    }

    public StudentResponse updateStudentCourseResults(String studentId, List<String> additionalCourseCodes) {

        validateNotNull(studentId);
        validateNotNull(additionalCourseCodes);

        //get the student object
        Student student = getStudentByStudentId(studentId);

        //get the recorded course codes
        List<String> recordedCourseCodes = student.getCourseResults()
                .stream()
                .map(CourseResult::getCourseCode)
                .toList();

        //compare with the additional course codes and remove duplicates
        additionalCourseCodes = additionalCourseCodes.stream()
                .filter(
                        courseCode -> !recordedCourseCodes.contains(courseCode)
                ).toList();

        //get the course results for the additional course codes
        List<CourseResult> courseResults = courseResultService.getListOfNCourseResults(additionalCourseCodes);
        student.setCourseResults(courseResults);

        studentRepo.save(student);

        return transferData(student, new StudentResponse());
    }

    public List<StudentResponse> getAllStudents() {

        return studentRepo.findAll()
                .stream()
                .map(StudentService::getStudentResponse)
                .toList();
    }

    public StudentResponse getStudentAsResponse(String studentId) {

        Student student = getStudentByStudentId(studentId);
        return getStudentResponse(student);
    }

    public Student getStudentByStudentId(String studentId) {

        return studentRepo.findStudentByStudentId(studentId).orElseThrow(() ->
                new ResourceNotFoundException("the student with ID: " + studentId + " was not found"));
    }

    public List<CourseResultResponse> getStudentCourseResults(String studentId) {
        Student student = getStudentByStudentId(studentId);
        return student.getCourseResults()
                .stream()
                .map(courseResult ->
                        transferData(courseResult, new CourseResultResponse())
                ).toList();
    }

    public void deleteStudent(String studentId) {

        studentRepo.deleteByStudentId(studentId);
    }


    ///  ======================================== helper methods ==================================================
    @NonNull
    private static StudentResponse getStudentResponse(Student student) {
        //Build the student response object
        StudentResponse studentResponse = transferData(student, new StudentResponse());

        //Convert CourseResult to CourseResultResponse
        List<CourseResultResponse> courseResultResponseList = student.getCourseResults()
                .stream()
                .map(courseResult ->
                        transferData(courseResult, new CourseResultResponse())
                ).toList();

        //update the student response object with the course result response list
        studentResponse.setCourseResultsResponse(courseResultResponseList);

        return studentResponse;
    }

    @NonNull
    private StudentResponse processStudentRequest(StudentRequest dto, Student student) {
        //use course codes provided to set the course results
        List<CourseResult> courseResults = courseResultService.getListOfNCourseResults(dto.getCourseCodes());
        student.setCourseResults(courseResults);

        //save the student
        studentRepo.save(student);

        return getStudentResponse(student);
    }


}