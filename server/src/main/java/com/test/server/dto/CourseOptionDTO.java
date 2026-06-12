package com.test.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CourseOptionDTO {
    private Long id;
    private String courseName;
}
