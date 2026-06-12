package com.test.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WarningDTO {
    private String type;
    private long count;
}
