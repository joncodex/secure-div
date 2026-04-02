package com.enzelascripts.securediv.service;

import com.enzelascripts.securediv.entity.CourseResult;
import com.enzelascripts.securediv.entity.Student;
import com.enzelascripts.securediv.exception.BadInputException;
import com.enzelascripts.securediv.exception.ResourceNotFoundException;
import com.enzelascripts.securediv.repository.CourseResultRepo;
import com.enzelascripts.securediv.repository.StudentRepo;
import com.enzelascripts.securediv.request.CourseResultRequest;
import com.enzelascripts.securediv.response.CourseResultResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

        CourseResult courseResult = transferData(dto, new CourseResult());
        Student student =  studentRepo.findStudentByStudentId(dto.getStudentId())
                .orElseThrow(() ->
                        new ResourceNotFoundException (
                                "Student with id " + dto.getStudentId() + " does not exist")
        );
        courseResult.setStudent(student);

        courseResultRepo.save(courseResult);

        return transferData(courseResult, new CourseResultResponse());
    }

    public List<CourseResult> getListOfNCourseResults(List<String> courseCodes) {
        return courseResultRepo
                .findCourseResultsByCourseCodeIn(courseCodes);
    }

    public String compareCourseResultAndCourseCodes(List<CourseResultResponse> courseResultsResponse, List<String> courseCodes) {

        //throw exception if the list of course results is null
        if (courseResultsResponse == null)
            throw new BadInputException("List of course results cannot be null");

        //Inform about empty course result list
        if (courseResultsResponse.isEmpty())
            return "The Course Result list is empty.";

        //inform about incomplete course result list if some initial course codes were invalid
        List<String> invalidCourseCodes = new ArrayList<>();
        if (courseResultsResponse.size() < courseCodes.size()){
            invalidCourseCodes = courseCodes
                    .stream()
                    .filter(courseCode->
                            courseResultsResponse
                                    .stream()
                                    .noneMatch(courseResult->
                                            courseResult.getCourseCode()
                                                    .equalsIgnoreCase(courseCode))
                    ).toList();

        }

        //return list of all invalid course codes
        return "Invalid Course Code(s): [" + invalidCourseCodes +
                "] Enter the correct course codes to record these courses";
    }

    public CourseResult getCourseResultById(long id) {

        return courseResultRepo.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Course result with id " + id + " not found")
        );
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

    @NonNull
    private CourseResult getCourseResultFromDB(String courseCode) {
        return courseResultRepo
                .findCourseResultByCourseCode(courseCode)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "the course result with course code: " + courseCode + " was not found"));
    }


}
