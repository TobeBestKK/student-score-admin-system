package com.test.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CourseRankDTO {
    private Long studentId;
    private String name;
    private String className;
    private BigDecimal score;
}