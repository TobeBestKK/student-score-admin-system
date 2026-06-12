package com.test.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class DashboardStatsDTO {
    private long courseCount;
    private long studentCount;
    private BigDecimal averageScore;
    private long failingCount;
    private BigDecimal maxScore;
    private BigDecimal passRate;
}
