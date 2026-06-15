package com.test.server.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ScoreRecordCreateDTO {

    @NotNull(message = "学生ID不能为空")
    private Long studentId;

    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    @NotNull(message = "成绩不能为空")
    @DecimalMin(value = "0", message = "成绩不能低于0")
    @DecimalMax(value = "100", message = "成绩不能高于100")
    private BigDecimal scoreValue;

    @NotBlank(message = "考试类型不能为空")
    private String examType;

    private String remark;
}
