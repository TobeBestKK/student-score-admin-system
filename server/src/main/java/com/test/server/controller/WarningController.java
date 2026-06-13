package com.test.server.controller;

import com.test.server.dto.StudentWarningsDTO;
import com.test.server.service.WarningService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warnings")
@RequiredArgsConstructor
public class WarningController {

    private final WarningService warningService;

    @GetMapping
    public ResponseEntity<List<StudentWarningsDTO>> getWarnings(
            @RequestParam(required = false) Long grade,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String type) {
        return ResponseEntity.ok(warningService.calculateAllWarnings(grade, classId, level, type));
    }

    @GetMapping("/class/{classId}")
    public ResponseEntity<List<StudentWarningsDTO>> getClassWarnings(@PathVariable Long classId) {
        return ResponseEntity.ok(warningService.calculateClassWarnings(classId));
    }
}
