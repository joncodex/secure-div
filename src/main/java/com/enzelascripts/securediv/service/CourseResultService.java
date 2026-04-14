package com.enzelascripts.securediv.service;

import com.enzelascripts.securediv.entity.CourseResult;
import com.enzelascripts.securediv.entity.Student;
import com.enzelascripts.securediv.exception.ResourceNotFoundException;
import com.enzelascripts.securediv.repository.CourseResultRepo;
import com.enzelascripts.securediv.repository.StudentRepo;
import com.enzelascripts.securediv.request.CourseResultRequest;
import com.enzelascripts.securediv.response.CourseResultResponse;
import com.enzelascripts.securediv.util.GradeCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.enzelascripts.securediv.util.Utility.transferData;
import static com.enzelascripts.securediv.util.Utility.validateNotNull;

@Service
public class CourseResultService {

//  ============================================== Fields ==================================================
    @Autowired
    private CourseResultRepo courseResultRepo;
    @Autowired
    private StudentRepo studentRepo;
    @Autowired
    private GradeCalculator gradeCalculator;


//  ======================================= public methods ==================================================
    public String createCourseResult(CourseResultRequest dto){

        //Confirm that the student exists
        Student student = getStudent(dto.getStudentId());

        CourseResult courseResult = transferData(dto, new CourseResult());
        courseResult.setStudent(student);
        courseResultRepo.save(courseResult);

        return "Course result created";

    }

    public CourseResultResponse update(String studentId, String courseCode, CourseResultRequest dto){
        validateNotNull(dto);
        validateNotNull(studentId);
        validateNotNull(courseCode);

        Student student = getStudent(studentId);
        Long id = student.getId();
        CourseResult courseResult = getCourseResultByStudentIdAndCourseCode(id, courseCode);

        //ensure the dto has the correct studentId and courseCode
        dto.setStudentId(studentId);
        dto.setCourseCode(courseCode);
        transferData(dto, courseResult);

        return getCourseResultResponse(courseResult);
    }

    public void delete(String studentId, String courseCode){

        Student student = getStudent(studentId);
        CourseResult courseResult = getCourseResultByStudentIdAndCourseCode(student.getId(), courseCode);

        courseResultRepo.delete(courseResult);
    };

    public List<CourseResult> getCourseResultsByStudentId(String studentId){
        validateNotNull(studentId);
        Student student = getStudent(studentId);
        Long id = student.getId();

        return courseResultRepo.getCourseResultsByStudent_Id(id);
    }

    public CourseResultResponse getCourseResultResponseByCourseCode(String studentId, String courseCode){

        validateNotNull(courseCode);
        validateNotNull(studentId);

        Student student = getStudent(studentId);
        Long id = student.getId();

        CourseResult courseResult = getCourseResultByStudentIdAndCourseCode(id, courseCode);

        return getCourseResultResponse(courseResult);
    }


//  ======================================= helper methods ===================================================
public CourseResultResponse getCourseResultResponse(CourseResult result){

        //null check
        validateNotNull(result);

        //transfer data
        CourseResultResponse response = transferData(result, new CourseResultResponse());

        //update the studentId, grade point and quality point
        response.setStudentId(result.getStudent().getStudentId());
        response.setGradePoint(getGradePoint(result.getScore()));
        response.setQualityPoint((response.getGradePoint() * response.getCourseUnit()));
        
        return response;
        
    }

    @NonNull
    private Student getStudent(String dto) {
        return studentRepo
                .findStudentByStudentId(dto)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Student with ID: " + dto + " not found"));
    }

    private double getGradePoint(int score){

        return gradeCalculator.calculateGradePoint(score);
    }

    private CourseResult getCourseResultByStudentIdAndCourseCode(@NonNull Long id, @NonNull String courseCode) {
        return courseResultRepo
                .findCourseResultByStudent_IdAndCourseCode(id, courseCode)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "the course result with course code: " + courseCode + " was not found"));
    }


//  ======================================= inner classes ==================================================


}
