package com.test.server.controller;

import com.test.server.dto.ScoreRecordCreateDTO;
import com.test.server.dto.ScoreRecordDTO;
import com.test.server.dto.ScoreRecordUpdateDTO;
import com.test.server.service.ScoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scores")
@RequiredArgsConstructor
public class ScoreController {

    private final ScoreService scoreService;

    @GetMapping
    public ResponseEntity<Page<ScoreRecordDTO>> getScoreRecords(
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String examType,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(scoreService.getScoreRecords(
                academicYear, semester, courseId, examType, keyword, page, size));
    }

    @PostMapping
    public ResponseEntity<ScoreRecordDTO> createScoreRecord(
            @Valid @RequestBody ScoreRecordCreateDTO dto) {
        return ResponseEntity.ok(scoreService.createScoreRecord(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScoreRecordDTO> updateScoreRecord(
            @PathVariable Long id,
            @RequestBody ScoreRecordUpdateDTO dto) {
        return ResponseEntity.ok(scoreService.updateScoreRecord(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteScoreRecord(@PathVariable Long id) {
        scoreService.deleteScoreRecord(id);
        return ResponseEntity.ok().build();
    }
}
