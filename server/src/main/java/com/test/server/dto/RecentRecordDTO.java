package com.test.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class RecentRecordDTO {
    private Long id;
    private String studentName;
    private String courseName;
    private BigDecimal scoreValue;
    private String examType;
    private LocalDateTime createTime;
}
