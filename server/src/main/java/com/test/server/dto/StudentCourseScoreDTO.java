package com.test.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class StudentCourseScoreDTO {
    private Long courseId;
    private String courseName;
    private BigDecimal scoreValue;
    private String examType;
    private BigDecimal credit;
    private int classRank;
    private int classTotal;
    private int gradeRank;
    private int gradeTotal;
}
