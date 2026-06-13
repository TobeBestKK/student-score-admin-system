package com.test.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class ClassScoreStatsDTO {
    private BigDecimal averageScore;
    private BigDecimal passRate;
    private long failCount;
    private long totalStudents;
    private Map<String, Long> distribution;
    private List<CourseRankingDTO> courseRankings;

    @Data
    @AllArgsConstructor
    public static class CourseRankingDTO {
        private String courseName;
        private BigDecimal averageScore;
        private BigDecimal passRate;
        private long failCount;
    }
}
