package com.test.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StudentCreateDTO {
    @NotBlank(message = "学号不能为空")
    private String studentNo;

    @NotBlank(message = "姓名不能为空")
    private String name;

    private Integer gender;

    @NotNull(message = "班级ID不能为空")
    private Long classId;

    private String phone;
    private String email;
    private Integer enrollmentYear;
}