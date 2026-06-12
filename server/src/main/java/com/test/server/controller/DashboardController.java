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
}
