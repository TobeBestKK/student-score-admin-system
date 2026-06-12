package com.test.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SemesterOptionDTO {
    private String academicYear;
    private String semester;
    private String label;
}
