package com.test.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StudentProfileDTO {
    private Long id;
    private String name;
    private String studentNo;
    private String className;
    private String major;
    private Integer grade;
    private Integer enrollmentYear;
}
