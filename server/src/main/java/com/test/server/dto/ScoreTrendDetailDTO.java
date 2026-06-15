package com.test.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class ScoreTrendDetailDTO {

    private List<CourseTrendItem> courseTrends;
    private List<SemesterSummary> semesterSummaries;

    @Data
    @AllArgsConstructor
    public static class CourseTrendItem {
        private String courseName;
        private List<ExamScore> exams;
        private BigDecimal latestChange;
        private String trendDirection;
    }

    @Data
    @AllArgsConstructor
    public static class ExamScore {
        private String examLabel;
        private String academicYear;
        private String semester;
        private String examType;
        private BigDecimal score;
        private BigDecimal classAvg;
        private BigDecimal gradeAvg;
    }

    @Data
    @AllArgsConstructor
    public static class SemesterSummary {
        private String label;
        private BigDecimal totalScore;
        private BigDecimal classAvgTotal;
        private BigDecimal gradeAvgTotal;
        private int courseCount;
    }
}
