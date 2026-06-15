package com.test.server.dto;

import lombok.Data;

@Data
public class ProfileDTO {
    private Long id;
    private String username;
    private String name;
    private String email;
    private String phone;
    private String position;
    private String studentId;
}
