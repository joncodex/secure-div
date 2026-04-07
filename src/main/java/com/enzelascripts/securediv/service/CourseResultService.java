package com.enzelascripts.securediv.service;

import com.enzelascripts.securediv.entity.CourseResult;
import com.enzelascripts.securediv.entity.Student;
import com.enzelascripts.securediv.exception.ResourceNotFoundException;
import com.enzelascripts.securediv.repository.CourseResultRepo;
import com.enzelascripts.securediv.repository.StudentRepo;
import com.enzelascripts.securediv.request.CourseResultRequest;
import com.enzelascripts.securediv.response.CourseResultResponse;
import com.enzelascripts.securediv.util.GradeCalculator;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.enzelascripts.securediv.util.Utility.transferData;
import static com.enzelascripts.securediv.util.Utility.validateNotNull;

@Service
public class CourseResultService {

//  ============================================== Fields ==================================================
    private final int cgpaScale = 7;
    @Autowired
    private CourseResultRepo courseResultRepo;
    @Autowired
    private StudentRepo studentRepo;
    @Autowired
    private GradeCalculator gradeCalculator;


//  ======================================= public methods ==================================================
    public CourseResultResponse createCourseResult(CourseResultRequest dto){

        //Confirm that the student exists
        Student student = getStudent(dto.getStudentId());

        CourseResult courseResult = transferData(dto, new CourseResult());
        courseResult.setStudent(student);
        courseResultRepo.save(courseResult);

        return getCourseResultResponse(courseResult);

    }

    public CourseResultResponse update(CourseResultRequest dto){
        validateNotNull(dto);

        Student student = getStudent(dto.getStudentId());
        Long id = student.getId();
        CourseResult courseResult = getCourseResultByStudentIdAndCourseCode(id, dto.getCourseCode());
        transferData(dto, courseResult);

        return getCourseResultResponse(courseResult);
    }

    public void delete(String StudentId, String courseCode){

        Student student = getStudent(StudentId);
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

    public CourseResultSummary getCourseResultSummary(String studentId){

        return new CourseResultSummary(studentId);
    }


//  ======================================= helper methods ===================================================
    private CourseResultResponse getCourseResultResponse(CourseResult result){

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

    private double getGradePoint(double score){

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
    public class CourseResultSummary{
        private final List<CourseResult> courseResults;

        private double totalGradePoints = 0;

        private int totalCourseUnits = 0;
        @Getter
        private final int cgpaScale = GradeCalculator.CGPA_SCALE;


//  ======================================= constructor ==================================================
        public CourseResultSummary(String studentId){

            this.courseResults = getCourseResultsByStudentId(studentId);
        }


//  ======================================= getter methods ==================================================
        public List<CourseResultResponse> getCourseResultResponseList() {

            return courseResults.stream()
                    .map(CourseResultService.this::getCourseResultResponse)
                    .toList();
        }

        public double getTotalGradePoints() {

            for(CourseResultResponse result: getCourseResultResponseList())
                totalGradePoints += result.getGradePoint();

            return totalGradePoints;
        }

        public double getCgpa() {

            return (getTotalGradePoints() / getTotalCourseUnits());
        }

        public int getTotalCourseUnits() {

            for(CourseResultResponse result: getCourseResultResponseList())
                totalCourseUnits += result.getCourseUnit();

            return totalCourseUnits;
        }

        public String getClassOfDegree() {

            return gradeCalculator.getClassOfDegree(getCgpa());
        }

    }


}
