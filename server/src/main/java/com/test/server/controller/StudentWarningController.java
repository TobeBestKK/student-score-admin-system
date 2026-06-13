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
    public ResponseEntity<StudentWarningsDTO> getStudentWarnings(@RequestParam Long studentId) {
        return ResponseEntity.ok(warningService.calculateStudentWarnings(studentId));
    }
}
