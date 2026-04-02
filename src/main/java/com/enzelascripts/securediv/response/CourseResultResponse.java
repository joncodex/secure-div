package com.enzelascripts.securediv.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseResultResponse {

    private String courseCode;
    private String courseTitle;

    private int creditUnit;
    private int score;

    private double grade;
    private double gradePoint;

    private int level;

    private String semester;
    private String session;
}
