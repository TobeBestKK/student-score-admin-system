package com.test.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class WarningItemDTO {
    private String type;
    private String level;
    private String courseName;
    private String currentValue;
    private String threshold;
    private String reason;
    private List<ScoreHistoryDTO> history;
}
