package com.test.server.controller;

import com.test.server.dto.*;
import com.test.server.service.ClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class ClassController {

    private final ClassService classService;

    @GetMapping
    public ResponseEntity<List<ClassListDTO>> getClasses(
            @RequestParam(required = false) Integer grade,
            @RequestParam(required = false) String major,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(classService.getClasses(grade, major, keyword));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClassDetailDTO> getClassDetail(@PathVariable Long id) {
        return ResponseEntity.ok(classService.getClassDetail(id));
    }

    @GetMapping("/{id}/students")
    public ResponseEntity<List<ClassStudentDTO>> getClassStudents(
            @PathVariable Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String studentNo,
            @RequestParam(required = false) Boolean hasWarning) {
        return ResponseEntity.ok(classService.getClassStudents(id, name, studentNo, hasWarning));
    }

    @GetMapping("/{id}/score-stats")
    public ResponseEntity<ClassScoreStatsDTO> getClassScoreStats(@PathVariable Long id) {
        return ResponseEntity.ok(classService.getClassScoreStats(id));
    }
}
