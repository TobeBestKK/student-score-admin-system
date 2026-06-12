package com.test.server.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sys_role")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_name", length = 50)
    private String roleName;

    @Column(name = "role_code", length = 50)
    private String roleCode;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private Integer status;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "is_deleted")
    private Integer isDeleted;
}