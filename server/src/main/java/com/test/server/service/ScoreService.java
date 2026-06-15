package com.test.server.service;

import com.test.server.dto.ScoreRecordCreateDTO;
import com.test.server.dto.ScoreRecordDTO;
import com.test.server.dto.ScoreRecordUpdateDTO;
import com.test.server.entity.Course;
import com.test.server.entity.ScoreRecord;
import com.test.server.entity.Student;
import com.test.server.repository.CourseRepository;
import com.test.server.repository.ScoreRecordRepository;
import com.test.server.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScoreService {

    private final ScoreRecordRepository scoreRecordRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public Page<ScoreRecordDTO> getScoreRecords(
            String academicYear, String semester, Long courseId,
            String examType, String keyword, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> result = scoreRecordRepository.findScoreRecordsWithFilters(
                academicYear, semester, courseId, examType, keyword, pageable);

        return result.map(row -> {
            Long id = ((Number) row[0]).longValue();
            Long studentId = ((Number) row[1]).longValue();
            String studentName = (String) row[2];
            String studentNo = (String) row[3];
            String className = (String) row[4];
            Long cId = ((Number) row[5]).longValue();
            String courseName = (String) row[6];
            BigDecimal scoreValue = (BigDecimal) row[7];
            String eType = (String) row[8];
            String remark = (String) row[9];
            LocalDateTime createTime = row[10] instanceof java.sql.Timestamp
                    ? ((java.sql.Timestamp) row[10]).toLocalDateTime()
                    : (LocalDateTime) row[10];

            String grade = getGradeLabel(scoreValue);
            return new ScoreRecordDTO(id, studentId, studentName, studentNo, className,
                    cId, courseName, scoreValue, eType, remark, grade, createTime);
        });
    }

    public ScoreRecordDTO createScoreRecord(ScoreRecordCreateDTO dto) {
        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new RuntimeException("学生不存在"));
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new RuntimeException("课程不存在"));

        ScoreRecord record = new ScoreRecord();
        record.setStudentId(dto.getStudentId());
        record.setCourseId(dto.getCourseId());
        record.setScoreValue(dto.getScoreValue());
        record.setExamType(dto.getExamType());
        record.setRemark(dto.getRemark());
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        record.setIsDeleted(0);

        ScoreRecord saved = scoreRecordRepository.save(record);

        return new ScoreRecordDTO(
                saved.getId(),
                student.getId(),
                student.getName(),
                student.getStudentNo(),
                student.getClassInfo().getClassName(),
                course.getId(),
                course.getCourseName(),
                saved.getScoreValue(),
                saved.getExamType(),
                saved.getRemark(),
                getGradeLabel(saved.getScoreValue()),
                saved.getCreateTime()
        );
    }

    public ScoreRecordDTO updateScoreRecord(Long id, ScoreRecordUpdateDTO dto) {
        ScoreRecord record = scoreRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("成绩记录不存在"));

        if (dto.getScoreValue() != null) {
            record.setScoreValue(dto.getScoreValue());
        }
        if (dto.getExamType() != null) {
            record.setExamType(dto.getExamType());
        }
        if (dto.getRemark() != null) {
            record.setRemark(dto.getRemark());
        }
        record.setUpdateTime(LocalDateTime.now());

        ScoreRecord saved = scoreRecordRepository.save(record);

        Student student = saved.getStudent();
        Course course = saved.getCourse();

        return new ScoreRecordDTO(
                saved.getId(),
                student.getId(),
                student.getName(),
                student.getStudentNo(),
                student.getClassInfo().getClassName(),
                course.getId(),
                course.getCourseName(),
                saved.getScoreValue(),
                saved.getExamType(),
                saved.getRemark(),
                getGradeLabel(saved.getScoreValue()),
                saved.getCreateTime()
        );
    }

    public void deleteScoreRecord(Long id) {
        ScoreRecord record = scoreRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("成绩记录不存在"));
        record.setIsDeleted(1);
        record.setUpdateTime(LocalDateTime.now());
        scoreRecordRepository.save(record);
    }

    private String getGradeLabel(BigDecimal score) {
        if (score == null) return "未知";
        if (score.compareTo(BigDecimal.valueOf(90)) >= 0) return "优秀";
        if (score.compareTo(BigDecimal.valueOf(80)) >= 0) return "良好";
        if (score.compareTo(BigDecimal.valueOf(70)) >= 0) return "中等";
        if (score.compareTo(BigDecimal.valueOf(60)) >= 0) return "及格";
        return "不及格";
    }
}
