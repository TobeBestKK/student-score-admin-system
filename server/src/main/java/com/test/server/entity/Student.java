package com.test.server.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "student")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "student_no", nullable = false, length = 30)
    private String studentNo;

    @Column(nullable = false, length = 50)
    private String name;

    @Column
    private Integer gender;

    @Column(name = "class_id", nullable = false)
    private Long classId;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(name = "enrollment_year")
    private Integer enrollmentYear;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "is_deleted")
    private Integer isDeleted;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "class_id", insertable = false, updatable = false)
    private ClassInfo classInfo;
}