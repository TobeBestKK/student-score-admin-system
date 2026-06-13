package com.test.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class StudentStatsDTO {
    private BigDecimal totalScore;
    private BigDecimal averageScore;
    private BigDecimal earnedCredit;
    private int failCount;
    private int classRank;
    private int classTotal;
    private int gradeRank;
    private int gradeTotal;
    private BigDecimal totalCredit;
    private boolean creditWarning;
}
