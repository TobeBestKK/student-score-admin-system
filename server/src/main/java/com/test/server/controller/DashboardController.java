package com.test.server.controller;

import com.test.server.dto.*;
import com.test.server.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/courses")
    public ResponseEntity<List<CourseOptionDTO>> getCourses() {
        return ResponseEntity.ok(dashboardService.getCourseOptions());
    }

    @GetMapping("/semesters")
    public ResponseEntity<List<SemesterOptionDTO>> getSemesters() {
        return ResponseEntity.ok(dashboardService.getSemesterOptions());
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getStats(
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String semester) {
        return ResponseEntity.ok(dashboardService.getStats(academicYear, semester));
    }

    @GetMapping("/distribution")
    public ResponseEntity<ScoreDistributionDTO> getDistribution(
            @RequestParam(required = false) Long courseId) {
        return ResponseEntity.ok(dashboardService.getScoreDistribution(courseId));
    }

    @GetMapping("/top5/subject")
    public ResponseEntity<List<TopStudentDTO>> getTop5BySubject(
            @RequestParam(required = false) Long courseId) {
        return ResponseEntity.ok(dashboardService.getTop5BySubject(courseId));
    }

    @GetMapping("/top5/total")
    public ResponseEntity<List<TopStudentDTO>> getTop5Total(
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String semester) {
        return ResponseEntity.ok(dashboardService.getTop5Total(academicYear, semester));
    }

    @GetMapping("/warnings")
    public ResponseEntity<List<WarningDTO>> getWarnings(
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String semester) {
        return ResponseEntity.ok(dashboardService.getWarnings(academicYear, semester));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<RecentRecordDTO>> getRecentRecords(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(dashboardService.getRecentRecords(limit));
    }

    // ========== 学生端接口 ==========

    @GetMapping("/student/profile")
    public ResponseEntity<StudentProfileDTO> getStudentProfile(@RequestParam Long studentId) {
        return ResponseEntity.ok(dashboardService.getStudentProfile(studentId));
    }

    @GetMapping("/student/scores")
    public ResponseEntity<List<StudentCourseScoreDTO>> getStudentScores(
            @RequestParam Long studentId,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String examType) {
        return ResponseEntity.ok(dashboardService.getStudentScores(studentId, academicYear, semester, examType));
    }

    @GetMapping("/student/stats")
    public ResponseEntity<StudentStatsDTO> getStudentStats(
            @RequestParam Long studentId,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String examType) {
        return ResponseEntity.ok(dashboardService.getStudentStats(studentId, academicYear, semester, examType));
    }

    @GetMapping("/student/trend")
    public ResponseEntity<ScoreTrendDTO> getScoreTrend(@RequestParam Long studentId) {
        return ResponseEntity.ok(dashboardService.getScoreTrend(studentId));
    }

    @GetMapping("/student/radar")
    public ResponseEntity<RadarChartDTO> getRadarData(
            @RequestParam Long studentId,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String examType) {
        return ResponseEntity.ok(dashboardService.getRadarData(studentId, academicYear, semester, examType));
    }

    // ========== 年级排名接口 ==========

    @GetMapping("/student/grade-total-ranking")
    public ResponseEntity<List<GradeTotalRankDTO>> getGradeTotalRanking(
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String examType) {
        return ResponseEntity.ok(dashboardService.getGradeTotalRanking(academicYear, semester, examType));
    }

    @GetMapping("/student/course-ranking")
    public ResponseEntity<List<CourseRankDTO>> getCourseRanking(
            @RequestParam String courseName,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String examType) {
        return ResponseEntity.ok(dashboardService.getCourseRanking(courseName, academicYear, semester, examType));
    }
}
