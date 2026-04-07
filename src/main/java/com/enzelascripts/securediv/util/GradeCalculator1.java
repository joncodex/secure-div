package com.enzelascripts.securediv.util;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Component
public class GradeCalculator1 {

    private final Map<Integer, GradingScale> scales = new java.util.HashMap<>();

    public GradeCalculator1() {
        initializeScales();
    }

    private void initializeScales() {
        // 4.0 scale (US standard)
        scales.put(4, new GradingScale(4, new TreeMap<>(Map.of(
            90, new Grade("A", 4.0),
            80, new Grade("B", 3.0),
            70, new Grade("C", 2.0),
            60, new Grade("D", 1.0),
            0,  new Grade("F", 0.0)
        ))));

        // 5.0 scale (Nigeria)
        scales.put(5, new GradingScale(5, new TreeMap<>(Map.of(
            70, new Grade("A", 5.0),
            60, new Grade("B", 4.0),
            50, new Grade("C", 3.0),
            45, new Grade("D", 2.0),
            40, new Grade("E", 1.0),
            0,  new Grade("F", 0.0)
        ))));

        // 7.0 scale
        scales.put(7, new GradingScale(
                7,
                new TreeMap<>(Map.of(
            70, new Grade("A", 7.0),
            65, new Grade("B", 6.0),
            60, new Grade("C", 5.0),
            55, new Grade("D", 4.0),
            50, new Grade("E", 3.0),
            45, new Grade("F", 0.0),
            0,  new Grade("F", 0.0)
        ))));
    }

    /**
     * Convert percentage score to grade point
     * @param percentage 0-100
     * @param scale 4, 5, 7, etc.
     */
    public GradeResult calculateGrade(double percentage, int scale) {
        GradingScale gradingScale = scales.get(scale);
        if (gradingScale == null) {
            throw new IllegalArgumentException("Unsupported scale: " + scale);
        }

        // TreeMap finds the floor entry (highest threshold <= percentage)
        Map.Entry<Integer, Grade> entry = gradingScale.getThresholds().floorEntry((int) percentage);
        
        if (entry == null) {
            entry = gradingScale.getThresholds().firstEntry();
        }

        Grade grade = entry.getValue();
        
        return new GradeResult(
            round(percentage, 1),
            grade.getLetter(),
            grade.getPoints(),
            scale
        );
    }

    /**
     * Calculate CGPA from list of scores
     */
    public double calculateCGPA(List<Score> scores, int scale) {
        double totalPoints = 0;
        int totalUnits = 0;

        for (Score score : scores) {
            GradeResult result = calculateGrade(score.getPercentage(), scale);
            totalPoints += result.getPoints() * score.getUnits();
            totalUnits += score.getUnits();
        }

        return totalUnits > 0 ? round(totalPoints / totalUnits, 2) : 0.0;
    }

    /**
     * Get class of degree
     */
    public String getClassOfDegree(double cgpa, int scale) {
        double percentage = (cgpa / scale) * 100;

        if (percentage >= 80) return "First-Class Honours";
        if (percentage >= 65) return "Second-Class Honours (Upper Division)";
        if (percentage >= 50) return "Second-Class Honours (Lower Division)";
        if (percentage >= 40) return "Third-Class Honours";
        return "Fail";
    }

    // Helper
    private double round(double value, int decimals) {
        double factor = Math.pow(10, decimals);
        return Math.round(value * factor) / factor;
    }

    // Inner classes
    @Getter
    @RequiredArgsConstructor
    public static class GradingScale {
        private final int maxScale;
        private final TreeMap<Integer, Grade> thresholds; // percentage -> grade
    }

    @Getter
    @RequiredArgsConstructor
    public static class Grade {
        private final String letter;
        private final double points;
    }

    @Getter
    @RequiredArgsConstructor
    public static class GradeResult {
        private final double percentage;
        private final String letterGrade;
        private final double points;
        private final int scale;

        public String getDisplay() {
            return String.format("%.1f%% = %s (%.1f/%d)", percentage, letterGrade, points, scale);
        }
    }

    @Getter
    @Builder
    public static class Score {
        private final String courseCode;
        private final String courseTitle;
        private final int units;
        private final double percentage;

        public static Score of(String code, String title, int units, double percentage) {
            return new Score(code, title, units, percentage);
        }
    }
}