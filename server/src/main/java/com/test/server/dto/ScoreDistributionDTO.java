package com.test.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ScoreDistributionDTO {
    private List<String> labels;
    private List<Long> counts;
}
