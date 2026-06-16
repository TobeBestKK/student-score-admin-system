CREATE DATABASE IF NOT EXISTS student_score_admin
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_general_ci;

USE student_score_admin;

-- 为了方便重复导入，先删除旧表
DROP TABLE IF EXISTS score_record;
DROP TABLE IF EXISTS course;
DROP TABLE IF EXISTS student;
DROP TABLE IF EXISTS class_info;
DROP TABLE IF EXISTS teacher;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS sys_role;

-- =========================
-- 1. 角色表
-- =========================
CREATE TABLE sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    description VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用，1启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',

    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';


-- =========================
-- 2. 用户表
-- =========================
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '账号状态：0禁用，1启用',
    last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',

    UNIQUE KEY uk_username (username),
    KEY idx_role_id (role_id),

    CONSTRAINT fk_user_role
        FOREIGN KEY (role_id)
        REFERENCES sys_role(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';


-- =========================
-- 3. 教师表
-- =========================
CREATE TABLE teacher (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '教师ID',
    user_id BIGINT DEFAULT NULL COMMENT '关联用户ID',
    teacher_no VARCHAR(30) NOT NULL COMMENT '教师工号',
    name VARCHAR(50) NOT NULL COMMENT '教师姓名',
    gender TINYINT DEFAULT 0 COMMENT '性别：0未知，1男，2女',
    title VARCHAR(50) DEFAULT NULL COMMENT '职称',
    phone VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',

    UNIQUE KEY uk_teacher_no (teacher_no),
    UNIQUE KEY uk_teacher_user_id (user_id),
    KEY idx_teacher_name (name),

    CONSTRAINT fk_teacher_user
        FOREIGN KEY (user_id)
        REFERENCES sys_user(id)
        ON UPDATE CASCADE
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师表';


-- =========================
-- 4. 班级表
-- =========================
CREATE TABLE class_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '班级ID',
    class_code VARCHAR(30) NOT NULL COMMENT '班级编号',
    class_name VARCHAR(100) NOT NULL COMMENT '班级名称',
    major VARCHAR(100) NOT NULL COMMENT '专业',
    grade YEAR NOT NULL COMMENT '年级',
    head_teacher_id BIGINT DEFAULT NULL COMMENT '班主任教师ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',

    UNIQUE KEY uk_class_code (class_code),
    KEY idx_major_grade (major, grade),
    KEY idx_head_teacher_id (head_teacher_id),

    CONSTRAINT fk_class_head_teacher
        FOREIGN KEY (head_teacher_id)
        REFERENCES teacher(id)
        ON UPDATE CASCADE
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级表';


-- =========================
-- 5. 学生表
-- =========================
CREATE TABLE student (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '学生ID',
    user_id BIGINT DEFAULT NULL COMMENT '关联用户ID',
    student_no VARCHAR(30) NOT NULL COMMENT '学号',
    name VARCHAR(50) NOT NULL COMMENT '学生姓名',
    gender TINYINT DEFAULT 0 COMMENT '性别：0未知，1男，2女',
    class_id BIGINT NOT NULL COMMENT '班级ID',
    phone VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    enrollment_year YEAR DEFAULT NULL COMMENT '入学年份',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',

    UNIQUE KEY uk_student_no (student_no),
    UNIQUE KEY uk_student_user_id (user_id),
    KEY idx_student_name (name),
    KEY idx_student_class_id (class_id),

    CONSTRAINT fk_student_user
        FOREIGN KEY (user_id)
        REFERENCES sys_user(id)
        ON UPDATE CASCADE
        ON DELETE SET NULL,

    CONSTRAINT fk_student_class
        FOREIGN KEY (class_id)
        REFERENCES class_info(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生表';


-- =========================
-- 6. 课程表
-- =========================
CREATE TABLE course (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '课程ID',
    course_code VARCHAR(30) NOT NULL COMMENT '课程编号',
    course_name VARCHAR(100) NOT NULL COMMENT '课程名称',
    credit DECIMAL(3,1) NOT NULL DEFAULT 0.0 COMMENT '学分',
    teacher_id BIGINT DEFAULT NULL COMMENT '任课教师ID',
    academic_year VARCHAR(20) DEFAULT NULL COMMENT '学年，例如2025-2026',
    semester VARCHAR(20) NOT NULL COMMENT '学期，例如第一学期、第二学期',
    course_type VARCHAR(50) DEFAULT NULL COMMENT '课程类型，例如必修、选修',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',

    UNIQUE KEY uk_course_code (course_code),
    KEY idx_course_name (course_name),
    KEY idx_course_teacher_id (teacher_id),
    KEY idx_course_semester (academic_year, semester),

    CONSTRAINT fk_course_teacher
        FOREIGN KEY (teacher_id)
        REFERENCES teacher(id)
        ON UPDATE CASCADE
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';


-- =========================
-- 7. 成绩表
-- =========================
CREATE TABLE score_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '成绩ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    score_value DECIMAL(5,2) NOT NULL COMMENT '成绩分数',
    exam_type VARCHAR(30) NOT NULL DEFAULT '期末' COMMENT '考试类型：平时、期中、期末、实验、综合',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',

    KEY idx_score_student_id (student_id),
    KEY idx_score_course_id (course_id),
    KEY idx_score_exam_type (exam_type),
    UNIQUE KEY uk_student_course_exam (student_id, course_id, exam_type),

    CONSTRAINT fk_score_student
        FOREIGN KEY (student_id)
        REFERENCES student(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT fk_score_course
        FOREIGN KEY (course_id)
        REFERENCES course(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT chk_score_value
        CHECK (score_value >= 0 AND score_value <= 150)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩表';