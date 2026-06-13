package com.test.server.service;

import com.test.server.dto.*;
import com.test.server.entity.ClassInfo;
import com.test.server.entity.Student;
import com.test.server.entity.Teacher;
import com.test.server.repository.ClassInfoRepository;
import com.test.server.repository.ScoreRecordRepository;
import com.test.server.repository.StudentRepository;
import com.test.server.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ClassService {

    private final ClassInfoRepository classInfoRepository;
    private final StudentRepository studentRepository;
    private final ScoreRecordRepository scoreRecordRepository;
    private final TeacherRepository teacherRepository;
    private final WarningService warningService;

    public List<ClassListDTO> getClasses(Integer grade, String major, String keyword) {
        List<ClassInfo> classes;
        if (keyword != null && !keyword.isEmpty()) {
            classes = classInfoRepository.findByKeyword(keyword);
        } else if (grade != null && major != null && !major.isEmpty()) {
            classes = classInfoRepository.findByGradeAndMajor(grade, major);
        } else if (grade != null) {
            classes = classInfoRepository.findByGrade(grade);
        } else if (major != null && !major.isEmpty()) {
            classes = classInfoRepository.findByMajor(major);
        } else {
            classes = classInfoRepository.findAll();
        }

        List<ClassListDTO> result = new ArrayList<>();
        for (ClassInfo ci : classes) {
            if (ci.getIsDeleted() != null && ci.getIsDeleted() == 1) continue;

            long studentCount = studentRepository.countByClassId(ci.getId());
            String headTeacherName = null;
            if (ci.getHeadTeacherId() != null) {
                headTeacherName = teacherRepository.findById(ci.getHeadTeacherId())
                        .map(Teacher::getName).orElse(null);
            }

            List<StudentWarningsDTO> warnings = warningService.calculateClassWarnings(ci.getId());
            long warningCount = warnings.stream()
                    .filter(w -> w.getMaxLevel() != null)
                    .count();

            result.add(new ClassListDTO(
                    ci.getId(), ci.getClassCode(), ci.getClassName(),
                    ci.getMajor(), ci.getGrade(), headTeacherName,
                    studentCount, warningCount
            ));
        }
        return result;
    }

    public ClassDetailDTO getClassDetail(Long classId) {
        ClassInfo ci = classInfoRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("班级不存在"));

        long studentCount = studentRepository.countByClassId(classId);
        String headTeacherName = null;
        if (ci.getHeadTeacherId() != null) {
            headTeacherName = teacherRepository.findById(ci.getHeadTeacherId())
                    .map(Teacher::getName).orElse(null);
        }

        return new ClassDetailDTO(
                ci.getId(), ci.getClassCode(), ci.getClassName(),
                ci.getMajor(), ci.getGrade(), headTeacherName, studentCount
        );
    }

    public List<ClassStudentDTO> getClassStudents(Long classId, String name, String studentNo, Boolean hasWarning) {
        List<Student> students = studentRepository.findByClassId(classId);
        List<ClassStudentDTO> result = new ArrayList<>();

        Map<Long, StudentWarningsDTO> warningMap = new HashMap<>();
        if (hasWarning != null && hasWarning) {
            List<StudentWarningsDTO> warnings = warningService.calculateClassWarnings(classId);
            for (StudentWarningsDTO w : warnings) {
                warningMap.put(w.getStudentId(), w);
            }
        }

        for (Student s : students) {
            if (s.getIsDeleted() != null && s.getIsDeleted() == 1) continue;
            if (name != null && !name.isEmpty() && !s.getName().contains(name)) continue;
            if (studentNo != null && !studentNo.isEmpty() && !s.getStudentNo().contains(studentNo)) continue;

            boolean studentHasWarning = false;
            String warningLevel = null;

            if (warningMap.containsKey(s.getId())) {
                StudentWarningsDTO sw = warningMap.get(s.getId());
                studentHasWarning = sw.getMaxLevel() != null;
                warningLevel = sw.getMaxLevel();
            } else if (hasWarning == null || !hasWarning) {
                StudentWarningsDTO sw = warningService.calculateStudentWarnings(s.getId());
                studentHasWarning = sw.getMaxLevel() != null;
                warningLevel = sw.getMaxLevel();
            }

            if (hasWarning != null && hasWarning && !studentHasWarning) continue;

            result.add(new ClassStudentDTO(
                    s.getId(), s.getStudentNo(), s.getName(), s.getGender(),
                    studentHasWarning, warningLevel
            ));
        }
        return result;
    }

    public ClassScoreStatsDTO getClassScoreStats(Long classId) {
        Object[] overview = scoreRecordRepository.findClassScoreOverview(classId);
        BigDecimal avgScore = overview[0] != null ? ((BigDecimal) overview[0]).setScale(1, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal passRate = overview[1] != null ? BigDecimal.valueOf(((Number) overview[1]).doubleValue()).setScale(1, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        long failCount = overview[2] != null ? ((Number) overview[2]).longValue() : 0;
        long totalStudents = overview[3] != null ? ((Number) overview[3]).longValue() : 0;

        List<Object[]> distRows = scoreRecordRepository.findClassScoreDistribution(classId);
        String[] allLabels = {"0-9", "10-19", "20-29", "30-39", "40-49", "50-59", "60-69", "70-79", "80-89", "90-100"};
        Map<String, Long> distribution = new LinkedHashMap<>();
        for (String label : allLabels) distribution.put(label, 0L);
        for (Object[] row : distRows) {
            distribution.put((String) row[0], ((Number) row[1]).longValue());
        }

        List<Object[]> courseRows = scoreRecordRepository.findClassCourseRankings(classId);
        List<ClassScoreStatsDTO.CourseRankingDTO> courseRankings = new ArrayList<>();
        for (Object[] row : courseRows) {
            String courseName = (String) row[0];
            BigDecimal courseAvg = row[1] != null ? ((BigDecimal) row[1]).setScale(1, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            BigDecimal coursePassRate = row[2] != null ? BigDecimal.valueOf(((Number) row[2]).doubleValue()).setScale(1, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            long courseFailCount = row[3] != null ? ((Number) row[3]).longValue() : 0;
            courseRankings.add(new ClassScoreStatsDTO.CourseRankingDTO(courseName, courseAvg, coursePassRate, courseFailCount));
        }

        return new ClassScoreStatsDTO(avgScore, passRate, failCount, totalStudents, distribution, courseRankings);
    }
}
