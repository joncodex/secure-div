package com.enzelascripts.securediv.util;

import com.enzelascripts.securediv.entity.CourseResult;
import com.enzelascripts.securediv.record.Grade;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.enzelascripts.securediv.util.Utility.validateNotNull;

@Component
@Getter
@Setter
public class GradeCalculator {
//  ============================================== Fields ==================================================
    public static final int CGPA_SCALE = 7;
    private Map<Integer, GradingScale> scales = new HashMap<>();


//  ============================================== public methods ===========================================
    public GradeCalculator() {

        //load all the scales available
        initializeScales();
    }

    public double calculateGradePoint(double score){

        validateNotNull(score);

        if(CGPA_SCALE <= 0 || score <= 0) return 0;
        if(CGPA_SCALE > 7 || score > 100) return 0;

        GradeCalculator gradeCalculator = new GradeCalculator();
        GradingScale grading = gradeCalculator.getScales().get(CGPA_SCALE);

        for (Map.Entry<Integer, Grade> entry : grading.getGradingScale().entrySet()) {
            if (entry.getKey() <= score) {
                return entry.getValue().point();
            }
        }

        return 0;

    }

    public double calculateCGPA(List<CourseResult> courseResults){

        double totalPoints = 0;
        int totalUnits = 0;

        for (CourseResult courseResult : courseResults) {

            double gradePoint = calculateGradePoint(courseResult.getScore());

            totalPoints += gradePoint * courseResult.getCourseUnit();
            totalUnits += courseResult.getCourseUnit();
        }

        if (totalUnits == 0) return 0;

        return Math.round((totalPoints / totalUnits) * 100.0) / 100.0;
    }

    public String getClassOfDegree(double cgpa) {
        double percentage = (cgpa / CGPA_SCALE) * 100;

        if (percentage >= 70) return "First-Class Honours";
        if (percentage >= 65) return "Second-Class Honours (Upper Division)";
        if (percentage >= 50) return "Second-Class Honours (Lower Division)";
        if (percentage >= 40) return "Third-Class Honours";
        return "Fail";
    }

//  ========================== helper methods ==================================================
    private void initializeScales() {
    scales.put(7, new GradingScale(get7PointGradeScale()));
    scales.put(5, new GradingScale(get5PointGradeScale()));
    scales.put(4, new GradingScale(get4PointGradeScale()));

}

    private TreeMap<Integer, Grade> get7PointGradeScale() {

        return new TreeMap<>(Map.of(
                70, new Grade("A", 7.0),
                65, new Grade("B+", 6.0),
                60, new Grade("B", 5.0),
                55, new Grade("C+", 4.0),
                50, new Grade("C", 3.0),
                45, new Grade("D", 2.0),
                40, new Grade("E", 1.0),
                0, new Grade("F", 0.0)

        ));

    }

    private TreeMap<Integer, Grade> get5PointGradeScale() {

        return new TreeMap<>(Map.of(
                80, new Grade("A", 5.0),
                70, new Grade("B", 4.0),
                60, new Grade("C", 3.0),
                50, new Grade("D", 2.0),
                40, new Grade("E", 1.0),
                0, new Grade("F", 0.0)
        ));
    }

    private TreeMap<Integer, Grade> get4PointGradeScale() {

        return new TreeMap<>(Map.of(
                80, new Grade("A", 4.0),
                70, new Grade("B", 3.0),
                60, new Grade("C", 2.0),
                50, new Grade("D", 1.0),
                0, new Grade("F", 0.0)
        ));

    }


//  =========================== inner class =====================================================
    @Getter
    @Setter
    @AllArgsConstructor
    public static class GradingScale {
        private final Map<Integer, Grade> gradingScale;
    }

}



        
