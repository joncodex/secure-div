package com.enzelascripts.securediv.service;

import com.enzelascripts.securediv.entity.CourseResult;
import com.enzelascripts.securediv.exception.ResourceNotFoundException;
import com.enzelascripts.securediv.record.Grade;
import com.enzelascripts.securediv.repository.CourseResultRepo;
import com.enzelascripts.securediv.repository.StudentRepo;
import com.enzelascripts.securediv.request.CourseResultRequest;
import com.enzelascripts.securediv.response.CourseResultResponse;
import com.enzelascripts.securediv.util.GradeCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.enzelascripts.securediv.util.Utility.transferData;
import static com.enzelascripts.securediv.util.Utility.validateNotNull;

@Service
public class CourseResultService {

//  ============================================== Fields ==================================================

    @Autowired
    private CourseResultRepo courseResultRepo;
    @Autowired
    private StudentRepo studentRepo;

//  ======================================= public methods ==================================================
    public CourseResultResponse createCourseResult(CourseResultRequest dto){

        //Confirm that the student exists
        boolean isStudentExist =  studentRepo.existsByStudentId(dto.getStudentId());
        if(!isStudentExist) throw new ResourceNotFoundException("Student with ID: " + dto.getStudentId() + " not found");

        CourseResult courseResult = transferData(dto, new CourseResult());
        courseResultRepo.save(courseResult);

        return transferData(courseResult, new CourseResultResponse());
    }

    public List<CourseResult> getListOfNCourseResults(List<String> courseCodes) {
        return courseResultRepo
                .findCourseResultsByCourseCodeIn(courseCodes);
    }

    public CourseResultResponse getCourseResultByCourseCode(String courseCode){

        validateNotNull(courseCode);
        CourseResult courseResult = getCourseResultFromDB(courseCode);

        return transferData(courseResult, new CourseResultResponse());
    }

    public CourseResult findCourseResultByCourseCode(String courseCode){

        validateNotNull(courseCode);
        return getCourseResultFromDB(courseCode);
    }

    public CourseResultResponse update(String courseCode, CourseResultRequest dto){
        validateNotNull(dto);
        CourseResult courseResult = getCourseResultFromDB(courseCode);
        transferData(dto, courseResult);
        return transferData(courseResult, new CourseResultResponse());
    }

    public void delete(String courseCode){

        courseResultRepo.deleteByCourseCode(courseCode);
    };

//  ======================================= helper methods ==================================================
    private CourseResultResponse getCourseResultResponse(CourseResult result){

        validateNotNull(result);
        CourseResultResponse response = transferData(result, new CourseResultResponse());
        response.setGradePoint(calculateGradePoint(7, result.getScore()));
        response.setQualityPoint((response.getGradePoint() * response.getCourseUnit()));
        
        return response;
        
    }

    private double calculateGradePoint(int cgpaScale, double score){

        validateNotNull(cgpaScale);
        validateNotNull(score);

        if(cgpaScale <= 0 || score <= 0) return 0;
        if(cgpaScale > 7 || score > 100) return 0;

        GradeCalculator gradeCalculator = new GradeCalculator();
        GradeCalculator.GradingScale grading = gradeCalculator.getScales().get(cgpaScale);

        for (Map.Entry<Integer, Grade> entry : grading.getGradingScale().entrySet()) {
            if (entry.getKey() <= score) {
                return entry.getValue().point();
            }
        }

        return 0;

    }


    @NonNull
    private CourseResult getCourseResultFromDB(String courseCode) {
        return courseResultRepo
                .findCourseResultByCourseCode(courseCode)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "the course result with course code: " + courseCode + " was not found"));
    }


}
