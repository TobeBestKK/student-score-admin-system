package com.test.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class GradeTotalRankDTO {
    private Long studentId;
    private String name;
    private String className;
    private BigDecimal totalScore;
    private BigDecimal averageScore;
    private List<CourseScoreDTO> courses;
}