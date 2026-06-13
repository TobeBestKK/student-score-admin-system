package com.test.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class StudentWarningsDTO {
    private Long studentId;
    private String studentNo;
    private String studentName;
    private String className;
    private String maxLevel;
    private int failCount;
    private List<WarningItemDTO> warnings;
}
