package com.test.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ScoreHistoryDTO {
    private String academicYear;
    private String semester;
    private String examType;
    private int score;
}
