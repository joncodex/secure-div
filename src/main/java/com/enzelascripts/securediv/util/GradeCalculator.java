package com.enzelascripts.securediv.util;

import com.enzelascripts.securediv.record.Grade;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class GradeCalculator {
    private Map<Integer, GradingScale> scales = new HashMap<>();

    public GradeCalculator() {

        //load all the scales available
        initializeScales();
    }

    private void initializeScales() {
        scales.put(7, new GradingScale(get7PointGradeScale()));
        scales.put(5, new GradingScale(get5PointGradeScale()));
        scales.put(4, new GradingScale(get4PointGradeScale()));

    }

    private Map<Integer, Grade> get7PointGradeScale() {

        return Map.of(
                70, new Grade("A", 7.0),
                65, new Grade("B+", 6.0),
                60, new Grade("B", 5.0),
                55, new Grade("C+", 4.0),
                50, new Grade("C", 3.0),
                45, new Grade("D", 2.0),
                40, new Grade("E", 1.0),
                0, new Grade("F", 0.0)

        );

    }

    private Map<Integer, Grade> get5PointGradeScale() {

        return Map.of(
                80, new Grade("A", 5.0),
                70, new Grade("B", 4.0),
                60, new Grade("C", 3.0),
                50, new Grade("D", 2.0),
                40, new Grade("E", 1.0),
                0, new Grade("F", 0.0)
        );
    }

    private Map<Integer, Grade> get4PointGradeScale() {

        return Map.of(
                80, new Grade("A", 4.0),
                70, new Grade("B", 3.0),
                60, new Grade("C", 2.0),
                50, new Grade("D", 1.0),
                0, new Grade("F", 0.0)
        );

    }


    @Getter
    @Setter
    @AllArgsConstructor
    public static class GradingScale {
        //        private int scores;
        private final Map<Integer, Grade> gradingScale;
    }
}
        
