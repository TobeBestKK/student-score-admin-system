package com.test.server.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sys_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Column(nullable = false)
    private Integer status;

    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "is_deleted")
    private Integer isDeleted;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", insertable = false, updatable = false)
    private Role role;
}