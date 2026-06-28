# 学生成绩管理系统

Student Score Admin System

一个基于 Spring Boot + Vue 3 构建的 full-stack 学生成绩管理系统，提供教师端和学生端双门户，支持成绩管理、班级管理、预警系统等功能。

## 技术栈

### 后端 (Backend)
- Java 17
- Spring Boot 3.x
- Spring Data JPA
- Spring Security
- MySQL
- Maven

### 前端 (Frontend)
- Vue 3
- TypeScript
- Vite
- Vue Router
- Pinia
- 国际化 (i18n)

## 功能特性

### 教师端 (Teacher Portal)
- **班级管理**: 查看班级列表、班级详情、学生列表、成绩统计
- **成绩管理**: 录入、编辑、删除学生成绩，支持分页查询
- **学生管理**: 学生信息的增删改查
- **数据看板**: 课程统计、成绩分布、TOP5学生、预警信息
- **预警系统**: 查看学生学业预警情况

### 学生端 (Student Portal)
- **个人仪表盘**: 查看个人成绩概览
- **成绩查询**: 按学期、考试类型查看各科成绩
- **排名信息**: 班级排名、年级排名
- **成绩趋势**: 各科目成绩变化趋势、雷达图分析
- **学业预警**: 查看个人预警信息

### 预警系统 (Warning System)
自动检测学生学业状态，预警类型包括：
- 低分预警 (单科成绩低于阈值)
- 平均分预警 (学期平均分低于标准)
- 不及格预警 (不及格科目数量过多)
- 学分预警 (已获学分不足)

## 项目结构

```
student-score-admin-system/
├── server/                    # 后端 Spring Boot 项目
│   ├── src/main/java/com/test/server/
│   │   ├── config/          # 配置类
│   │   ├── controller/      # 控制器
│   │   ├── dto/            # 数据传输对象
│   │   ├── entity/        # 实体类
│   │   ├── repository/    # 数据仓库
│   │   └── service/       # 业务服务
│   └── pom.xml
│
├── frontend/                 # 前端 Vue 3 项目
│   ├── src/
│   │   ├── api/           # API 接口
│   │   ├── components/    # UI 组件
│   │   ├── views/         # 页面视图
│   │   │   ├── teacher/   # 教师端页面
│   │   │   └── student/   # 学生端页面
│   │   ├── router/        # 路由配置
│   │   ├── i18n/          # 国际化
│   │   └── constants/    # 常量定义
│   └── package.json
│
├── schema.sql               # 数据库表结构
├── data.sql                 # 种子数据
└── README.md
```

## 快速启动

### 环境要求

- JDK 17+
- MySQL 5.7+
- Node.js 16+
- Maven 3.8+

### 1. 数据库初始化

```sql
-- 创建数据库
CREATE DATABASE student_score_admin CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 导入表结构和数据
USE student_score_admin;
SOURCE schema.sql;
SOURCE data.sql;
```

### 2. 启动后端

进入 server 目录：

```bash
# Windows
./mvnw.cmd spring-boot:run

# macOS / Linux
./mvnw spring-boot:run
```

后端默认运行在 http://localhost:8080

### 3. 启动前端

进入 frontend 目录：

```bash
# 安装依赖
npm install

# 开发模式
npm run dev

# 生产构建
npm run build
```

前端默认运行在 http://localhost:5173

## 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 教师 | teacher1 | password |
| 教师 | teacher2 | password |
| 学生 | student1 | password |
| 学生 | student2 | password |

## 国际化

系统支持中文 (zh-CN) 和英文 (en-US) 两种语言，可在登录后的设置页面中切换。

## 许可证

MIT License