package com.test.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class ScoreTrendDTO {
    private List<String> examLabels;
    private List<BigDecimal> studentScores;
    private List<BigDecimal> classAverages;
    private List<BigDecimal> gradeAverages;
}
