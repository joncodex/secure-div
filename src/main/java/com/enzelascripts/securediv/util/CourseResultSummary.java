package com.enzelascripts.securediv.util;

import com.enzelascripts.securediv.entity.CourseResult;
import com.enzelascripts.securediv.repository.CourseResultRepo;
import com.enzelascripts.securediv.repository.StudentRepo;
import com.enzelascripts.securediv.response.CourseResultResponse;
import com.enzelascripts.securediv.service.CourseResultService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope(scopeName = "prototype")
@Getter
public class CourseResultSummary{

    @Autowired
    private CourseResultRepo courseResultRepo;
    @Autowired
    private StudentRepo studentRepo;
    @Autowired
    private CourseResultService courseResultService;

    private List<CourseResult> courseResults;
    private List<CourseResultResponse> courseResultResponseList;
    private double totalQualityPoints;
    private double cgpa;
    private int totalCourseUnits;
    private String classOfDegree;

    private final int cgpaScale = GradeCalculator.CGPA_SCALE;


    //  ======================================= instance method ==================================================
    public void getInstance(String studentId){

        this.courseResults = courseResultService.getCourseResultsByStudentId(studentId);
        this.courseResultResponseList = getCourseResultResponseList();

        this.totalQualityPoints = getTotalQualityPoints();
        this.totalCourseUnits = getTotalCourseUnits();
        this.cgpa = getCgpa();
        this.classOfDegree = getClassOfDegree();

    }


    //  ======================================= getter methods ==================================================
    public List<CourseResultResponse> getCourseResultResponseList() {

        courseResultResponseList = courseResults.stream()
                .map(s-> courseResultService.getCourseResultResponse(s))
                .toList();

        System.out.println(courseResultResponseList);
        return courseResultResponseList;
    }

    public double getTotalQualityPoints() {
        totalQualityPoints = courseResultResponseList
                .stream()
                .mapToDouble(CourseResultResponse::getQualityPoint)
                .sum();

        return totalQualityPoints;

    }

    public int getTotalCourseUnits() {
        totalCourseUnits = courseResultResponseList
                .stream()
                .mapToInt(CourseResultResponse::getCourseUnit)
                .sum();

        return totalCourseUnits;
    }

    public double getCgpa() {

        int totalUnits = getTotalCourseUnits();
        if(totalUnits == 0) return 0;

        cgpa = Math.round(getTotalQualityPoints() / totalUnits * 100.0) / 100.0;

        return cgpa;
    }

    public String getClassOfDegree() {

        classOfDegree = getClassOfDegree(getCgpa());
        return classOfDegree;
    }

    public String getClassOfDegree(double cgpa) {
        double percentage = (cgpa / cgpaScale) * 100;

        if (percentage >= 70) return "First-Class Honours";
        if (percentage >= 65) return "Second-Class Honours (Upper Division)";
        if (percentage >= 50) return "Second-Class Honours (Lower Division)";
        if (percentage >= 40) return "Third-Class Honours";
        return "Fail";
    }


}