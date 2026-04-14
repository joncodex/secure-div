package com.enzelascripts.securediv.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseResultResponse {

    private String studentId;

    private String courseCode;
    private String courseTitle;

    private int courseUnit;
    private int score;

    private double gradePoint;
    private double qualityPoint;

    private int level;

    private String semester;
    private String session;
}

