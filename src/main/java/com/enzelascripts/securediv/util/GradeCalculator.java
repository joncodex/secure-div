package com.enzelascripts.securediv.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.enzelascripts.securediv.util.Utility.validateNotNull;

@Component
@Getter
@Setter
public class GradeCalculator {
//  ============================================== Fields ==================================================
    public static final int CGPA_SCALE = 4;
    private Map<Integer, GradingScale> scales = new HashMap<>();


//  ============================================== public methods ===========================================
    public GradeCalculator() {

        //load all the scales available
        initializeScales();
    }

    public double calculateGradePoint(int score){

        //validate score
        validateNotNull(score);
        if(score < 0 || score > 100) return 0;

        TreeMap<Integer, Grade> gradingScale =
                validateNotNull(scales.get(CGPA_SCALE).gradingScale, "grade scale not found");

        Map.Entry<Integer, Grade> entry =
                validateNotNull(gradingScale.floorEntry(score), "grade not found");

        return entry.getValue().point();

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
                0,  new Grade("F", 0.0)

        ));

    }

    private TreeMap<Integer, Grade> get5PointGradeScale() {

        return new TreeMap<>(Map.of(
                80, new Grade("A", 5.0),
                70, new Grade("B", 4.0),
                60, new Grade("C", 3.0),
                50, new Grade("D", 2.0),
                40, new Grade("E", 1.0),
                0,  new Grade("F", 0.0)
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
    @AllArgsConstructor
    public static class GradingScale {
        private final TreeMap<Integer, Grade> gradingScale;
    }

}



        
