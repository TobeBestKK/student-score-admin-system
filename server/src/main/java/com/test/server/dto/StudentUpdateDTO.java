package com.test.server.dto;

import lombok.Data;

@Data
public class StudentUpdateDTO {
    private String studentNo;
    private String name;
    private Integer gender;
    private Long classId;
    private String phone;
    private String email;
    private Integer enrollmentYear;
}