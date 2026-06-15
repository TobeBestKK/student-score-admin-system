package com.test.server.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ScoreRecordUpdateDTO {

    @DecimalMin(value = "0", message = "成绩不能低于0")
    @DecimalMax(value = "100", message = "成绩不能高于100")
    private BigDecimal scoreValue;

    private String examType;

    private String remark;
}
