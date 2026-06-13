package com.test.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClassDetailDTO {
    private Long id;
    private String classCode;
    private String className;
    private String major;
    private Integer grade;
    private String headTeacherName;
    private long studentCount;
}
