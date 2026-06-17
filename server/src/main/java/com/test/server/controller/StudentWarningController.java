package com.test.server.controller;

import com.test.server.dto.StudentWarningsDTO;
import com.test.server.service.WarningService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/warnings")
@RequiredArgsConstructor
public class StudentWarningController {

    private final WarningService warningService;

    @GetMapping
    public ResponseEntity<StudentWarningsDTO> getStudentWarnings(
            @RequestParam Long studentId,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String semester) {
        if (academicYear != null || semester != null) {
            return ResponseEntity.ok(warningService.calculateStudentWarnings(studentId, academicYear, semester));
        }
        return ResponseEntity.ok(warningService.calculateStudentWarnings(studentId));
    }
}
