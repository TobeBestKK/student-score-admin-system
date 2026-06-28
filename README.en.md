# Student Score Administration System

A full-stack student score management system built with Spring Boot + Vue 3, offering dual portals for teachers and students, supporting features such as score management, class management, and a warning system.

## Technology Stack

### Backend
- Java 17
- Spring Boot 3.x
- Spring Data JPA
- Spring Security
- MySQL
- Maven

### Frontend
- Vue 3
- TypeScript
- Vite
- Vue Router
- Pinia
- Internationalization (i18n)

## Features

### Teacher Portal
- **Class Management**: View class lists, class details, student lists, and score statistics
- **Score Management**: Add, edit, delete student scores; supports pagination
- **Student Management**: Create, read, update, and delete student information
- **Data Dashboard**: Course statistics, score distribution, top 5 students, warning alerts
- **Warning System**: View academic warning statuses for students

### Student Portal
- **Personal Dashboard**: View personal score overview
- **Score Inquiry**: View subject scores by semester and exam type
- **Rankings**: Class ranking and grade ranking
- **Score Trends**: Trends in subject scores and radar chart analysis
- **Academic Warning**: View personal warning notifications

### Warning System
Automatically monitors student academic status. Warning types include:
- Low Score Warning (individual subject score below threshold)
- Average Score Warning (semester average below standard)
- Fail Warning (excessive number of failed subjects)
- Credit Warning (insufficient earned credits)

## Project Structure

```
student-score-admin-system/
├── server/                    # Backend Spring Boot project
│   ├── src/main/java/com/test/server/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # Controllers
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── entity/        # Entity classes
│   │   ├── repository/    # Data repositories
│   │   └── service/       # Business services
│   └── pom.xml
│
├── frontend/                 # Frontend Vue 3 project
│   ├── src/
│   │   ├── api/           # API interfaces
│   │   ├── components/    # UI components
│   │   ├── views/         # Page views
│   │   │   ├── teacher/   # Teacher portal pages
│   │   │   └── student/   # Student portal pages
│   │   ├── router/        # Routing configuration
│   │   ├── i18n/          # Internationalization
│   │   └── constants/    # Constant definitions
│   └── package.json
│
├── schema.sql               # Database schema
├── data.sql                 # Seed data
└── README.md
```

## Quick Start

### Prerequisites

- JDK 17+
- MySQL 5.7+
- Node.js 16+
- Maven 3.8+

### 1. Database Initialization

```sql
-- Create database
CREATE DATABASE student_score_admin CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Import schema and data
USE student_score_admin;
SOURCE schema.sql;
SOURCE data.sql;
```

### 2. Start Backend

Navigate to the `server` directory:

```bash
# Windows
./mvnw.cmd spring-boot:run

# macOS / Linux
./mvnw spring-boot:run
```

The backend runs by default at http://localhost:8080

### 3. Start Frontend

Navigate to the `frontend` directory:

```bash
# Install dependencies
npm install

# Development mode
npm run dev

# Production build
npm run build
```

The frontend runs by default at http://localhost:5173

## Default Accounts

| Role   | Username  | Password |
|--------|-----------|----------|
| Teacher| teacher1  | password |
| Teacher| teacher2  | password |
| Student| student1  | password |
| Student| student2  | password |

## Internationalization

The system supports Chinese (zh-CN) and English (en-US). Language can be switched in the settings page after login.

## License

MIT License