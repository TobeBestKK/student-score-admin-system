package com.test.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TopStudentDTO {
    private int rank;
    private String name;
    private String className;
    private BigDecimal score;
}
