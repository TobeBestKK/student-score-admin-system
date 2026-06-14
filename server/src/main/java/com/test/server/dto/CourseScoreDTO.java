package com.test.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CourseScoreDTO {
    private String courseName;
    private BigDecimal scoreValue;
}