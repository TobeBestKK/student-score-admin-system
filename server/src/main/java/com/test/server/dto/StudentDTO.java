package com.test.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentDTO {
    private Long id;
    private String studentNo;
    private String name;
    private Integer gender;
    private Long classId;
    private String className;
    private String phone;
    private String email;
    private Integer enrollmentYear;
}