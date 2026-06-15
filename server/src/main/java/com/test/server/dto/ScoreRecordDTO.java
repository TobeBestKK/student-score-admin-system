package com.test.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ScoreRecordDTO {
    private Long id;
    private Long studentId;
    private String studentName;
    private String studentNo;
    private String className;
    private Long courseId;
    private String courseName;
    private BigDecimal scoreValue;
    private String examType;
    private String remark;
    private String grade;
    private LocalDateTime createTime;
}
