package com.test.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClassStudentDTO {
    private Long id;
    private String studentNo;
    private String name;
    private Integer gender;
    private boolean hasWarning;
    private String warningLevel;
}
