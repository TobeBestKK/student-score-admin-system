# 学生成绩分析管理系统 | Student Score Admin System

## 项目简介

基于前后端分离架构的学生成绩分析管理系统，支持教师和学生双角色。教师可管理班级、学生、课程及成绩，查看数据仪表盘和学业预警；学生可查询个人成绩、追踪学习趋势、查看排名和预警信息。

A full-stack student score analysis and management system with role-based access for teachers and students. Teachers can manage classes, students, courses, and scores with dashboard analytics and academic warnings. Students can view personal scores, track trends, check rankings, and receive warning alerts.

## 技术栈 | Tech Stack

### 前端 | Frontend

| 类别 | 技术 |
|---|---|
| 框架 | Vue 3 (Composition API, `<script setup>`) |
| 语言 | TypeScript |
| 构建工具 | Vite |
| 路由 | Vue Router |
| HTTP 客户端 | Axios |
| CSS 框架 | Tailwind CSS v4 |
| UI 组件 | Element Plus + shadcn-vue |
| 图表 | ECharts |
| 国际化 | vue-i18n (中文/英文) |
| 图标 | Lucide Vue |

### 后端 | Backend

| 类别 | 技术 |
|---|---|
| 框架 | Spring Boot 4.1.0 |
| 语言 | Java 17 |
| ORM | Spring Data JPA (Hibernate) |
| 数据库 | MySQL 8.4.4 |
| 构建工具 | Maven |

## 功能特性 | Features

### 教师端 | Teacher Portal

- **数据仪表盘** — 统计卡片、成绩分布图、Top5 排名、预警概览、最近成绩记录
- **班级管理** — 班级列表筛选、班级详情、学生名册、班级成绩统计、预警工作台
- **学生管理** — 学生增删改查、分页搜索、按年级/班级筛选
- **成绩管理** — 成绩记录增删改查、按学年/学期/课程/考试类型筛选
- **预警管理** — 全校预警查看、按等级/类型/班级筛选

### 学生端 | Student Portal

- **学业总览** — 统计卡片（总学分、平均分、班级/年级排名）、雷达图、成绩趋势、预警
- **成绩查询** — 课程成绩表、班级+年级排名、按学年/学期/考试类型筛选
- **趋势分析** — 各科成绩趋势线、期中/期末对比、学期汇总、进步/退步追踪
- **预警中心** — 个人预警详情、预警等级/类型、成绩历史、改进建议
- **排名查看** — 年级总分排名 + 各科排名

### 预警系统 | Warning System

| 等级 | 说明 |
|---|---|
| 普通提醒 | 单科成绩 60-65 分，或学期均分 < 65 |
| 重点关注 | 单科成绩 < 60 分 |
| 严重预警 | 累计不及格 >= 3 科，或总分排名后 30% |

## 项目结构 | Project Structure

```text
student-score-admin-system/
├── schema.sql                  # 数据库表结构 DDL
├── data.sql                    # 种子数据 (3个班级、9名教师、30名学生、54门课程、2160+ 条成绩)
│
├── frontend/                   # Vue 3 前端
│   ├── src/
│   │   ├── api/                # API 接口层 (Axios)
│   │   ├── router/             # 路由配置 (含权限守卫)
│   │   ├── views/
│   │   │   ├── teacher/        # 教师端页面 (7 个)
│   │   │   └── student/        # 学生端页面 (6 个)
│   │   ├── components/ui/      # shadcn-vue 组件
│   │   ├── i18n/               # 国际化 (zh-CN / en-US)
│   │   ├── hooks/              # 组合式函数
│   │   └── constants/          # 枚举常量
│   └── package.json
│
└── server/                     # Spring Boot 后端
    └── src/main/java/com/test/server/
        ├── controller/         # REST 控制器
        ├── service/            # 业务逻辑
        ├── repository/         # JPA 数据访问
        ├── entity/             # 实体类
        ├── dto/                # 数据传输对象
        └── config/             # 配置 (CORS、Security、预警阈值)
```

## 快速启动 | Quick Start

### 环境要求 | Prerequisites

- **Java** 17+
- **Maven** 3.6+ (或使用项目内置 `mvnw`)
- **MySQL** 8.x
- **Node.js** 18+

### 1. 数据库初始化 | Database Setup

```bash
# 创建数据库并导入表结构
mysql -u root -p < schema.sql

# 导入种子数据
mysql -u root -p < data.sql
```

数据库默认连接信息：
- Host: `localhost:3306`
- Database: `student_score_admin`
- Username: `root`
- Password: `root`

### 2. 启动后端 | Start Backend

```bash
cd server

# Windows
mvnw.cmd spring-boot:run

# macOS / Linux
./mvnw spring-boot:run
```

后端运行在 `http://localhost:8083`

### 3. 启动前端 | Start Frontend

```bash
cd frontend

npm install

# 开发模式
npm run dev

# 生产构建
npm run build

# 预览生产构建
npm run preview
```

前端开发服务器运行在 `http://localhost:5173` (默认)

## 默认账号 | Default Accounts

所有账号密码均为 `123456`。

| 角色 | 用户名 | 说明 |
|---|---|---|
| 管理员 | `admin` | 系统管理员 |
| 教师 | `T001` ~ `T009` | 9 名教师 |
| 学生 | `20260101` ~ `20260110` | 高三1班 |
| 学生 | `20260201` ~ `20260210` | 高三2班 |
| 学生 | `20260301` ~ `20260310` | 高三3班 |

## 国际化 | i18n

系统支持中文 (zh-CN) 和英文 (en-US)，默认中文，可在设置页面切换。

The system supports Chinese (zh-CN) and English (en-US). Default is Chinese. Switch language in Settings.