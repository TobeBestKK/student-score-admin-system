package com.test.server.service;

import com.test.server.config.WarningThresholdConfig;
import com.test.server.config.WarningThresholdConfig.WarningLevel;
import com.test.server.config.WarningThresholdConfig.WarningType;
import com.test.server.dto.ScoreHistoryDTO;
import com.test.server.dto.WarningItemDTO;
import com.test.server.dto.StudentWarningsDTO;
import com.test.server.entity.Course;
import com.test.server.entity.ScoreRecord;
import com.test.server.entity.Student;
import com.test.server.repository.CourseRepository;
import com.test.server.repository.ScoreRecordRepository;
import com.test.server.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WarningService {

    private final ScoreRecordRepository scoreRecordRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public StudentWarningsDTO calculateStudentWarnings(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("学生不存在"));

        List<ScoreRecord> records = scoreRecordRepository.findByStudentIdAndExamType(studentId, "期末");
        List<ScoreRecord> allRecords = scoreRecordRepository.findByStudentIdAllSemesters(studentId);
        return buildStudentWarnings(student, records, allRecords);
    }

    public StudentWarningsDTO calculateStudentWarnings(Long studentId, String academicYear, String semester) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("学生不存在"));

        List<ScoreRecord> records = scoreRecordRepository.findByStudentIdOrderedWithFilters(
                studentId, academicYear, semester, "期末");
        List<ScoreRecord> allRecords = scoreRecordRepository.findByStudentIdAllSemesters(studentId);
        return buildStudentWarnings(student, records, allRecords);
    }

    private StudentWarningsDTO buildStudentWarnings(Student student, List<ScoreRecord> records, List<ScoreRecord> allRecords) {
        Long studentId = student.getId();
        List<WarningItemDTO> warnings = new ArrayList<>();
        WarningLevel maxLevel = null;

        // Build course name to history map
        Map<String, List<ScoreHistoryDTO>> courseHistoryMap = new HashMap<>();
        for (ScoreRecord record : allRecords) {
            Course course = record.getCourse();
            String courseName = course.getCourseName();
            courseHistoryMap.computeIfAbsent(courseName, k -> new ArrayList<>())
                    .add(new ScoreHistoryDTO(
                            course.getAcademicYear(),
                            course.getSemester(),
                            record.getExamType(),
                            record.getScoreValue().intValue()
                    ));
        }

        // 1. 单科预警
        for (ScoreRecord record : records) {
            BigDecimal score = record.getScoreValue();
            Course course = record.getCourse();
            String courseName = course.getCourseName();
            String academicYear = course.getAcademicYear();
            String semester = course.getSemester();
            String timePrefix = academicYear + " " + semester + " ";
            List<ScoreHistoryDTO> history = courseHistoryMap.getOrDefault(courseName, Collections.emptyList());

            if (score.doubleValue() < WarningThresholdConfig.FAIL_SCORE) {
                String reason = timePrefix + "期末《" + courseName + "》" + score.intValue() + " 分，触发重点关注";
                warnings.add(new WarningItemDTO(
                        WarningType.LOW_SCORE.getLabel(),
                        WarningLevel.MODERATE.getLabel(),
                        courseName,
                        String.valueOf(score.intValue()),
                        "< 60",
                        reason,
                        history
                ));
                maxLevel = maxLevel == null ? WarningLevel.MODERATE : WarningLevel.max(maxLevel, WarningLevel.MODERATE);
            } else if (score.doubleValue() <= WarningThresholdConfig.LOW_SCORE_MAX) {
                String reason = timePrefix + "期末《" + courseName + "》" + score.intValue() + " 分，触发普通提醒";
                warnings.add(new WarningItemDTO(
                        WarningType.LOW_SCORE.getLabel(),
                        WarningLevel.NORMAL.getLabel(),
                        courseName,
                        String.valueOf(score.intValue()),
                        "60-65",
                        reason,
                        history
                ));
                maxLevel = maxLevel == null ? WarningLevel.NORMAL : WarningLevel.max(maxLevel, WarningLevel.NORMAL);
            }
        }

        // 2. 累计不及格门数
        int failCount = 0;
        for (ScoreRecord record : records) {
            if (record.getScoreValue().doubleValue() < WarningThresholdConfig.FAIL_SCORE) {
                failCount++;
            }
        }

        if (failCount >= WarningThresholdConfig.FAIL_COUNT_SEVERE_MIN) {
            String reason = "累计不及格 " + failCount + " 门，触发严重预警";
            warnings.add(new WarningItemDTO(
                    WarningType.CUMULATIVE_FAIL.getLabel(),
                    WarningLevel.SEVERE.getLabel(),
                    null,
                    String.valueOf(failCount),
                    ">= " + WarningThresholdConfig.FAIL_COUNT_SEVERE_MIN,
                    reason,
                    Collections.emptyList()
            ));
            maxLevel = WarningLevel.SEVERE;
        } else if (failCount >= 1) {
            String reason = "累计不及格 " + failCount + " 门，触发重点关注";
            warnings.add(new WarningItemDTO(
                    WarningType.CUMULATIVE_FAIL.getLabel(),
                    WarningLevel.MODERATE.getLabel(),
                    null,
                    String.valueOf(failCount),
                    "1-" + WarningThresholdConfig.FAIL_COUNT_MODERATE_MAX,
                    reason,
                    Collections.emptyList()
            ));
            maxLevel = maxLevel == null ? WarningLevel.MODERATE : WarningLevel.max(maxLevel, WarningLevel.MODERATE);
        }

        // 3. 学期平均分预警
        Map<String, List<BigDecimal>> semesterScores = new LinkedHashMap<>();
        for (ScoreRecord record : records) {
            Course course = record.getCourse();
            String key = course.getAcademicYear() + " " + course.getSemester();
            semesterScores.computeIfAbsent(key, k -> new ArrayList<>()).add(record.getScoreValue());
        }

        for (Map.Entry<String, List<BigDecimal>> entry : semesterScores.entrySet()) {
            List<BigDecimal> scores = entry.getValue();
            BigDecimal avg = BigDecimal.ZERO;
            for (BigDecimal s : scores) {
                avg = avg.add(s);
            }
            avg = avg.divide(BigDecimal.valueOf(scores.size()), 1, RoundingMode.HALF_UP);

            if (avg.doubleValue() < WarningThresholdConfig.SEMESTER_AVG_THRESHOLD) {
                String reason = entry.getKey() + " 平均分 " + avg + "，触发普通提醒";
                warnings.add(new WarningItemDTO(
                        WarningType.SEMESTER_AVG.getLabel(),
                        WarningLevel.NORMAL.getLabel(),
                        null,
                        avg.toString(),
                        "< " + WarningThresholdConfig.SEMESTER_AVG_THRESHOLD,
                        reason,
                        Collections.emptyList()
                ));
                maxLevel = maxLevel == null ? WarningLevel.NORMAL : WarningLevel.max(maxLevel, WarningLevel.NORMAL);
            }
        }

        // 4. 总分排名后 30% 预警
        Long classId = student.getClassId();
        List<Object[]> classRanking = scoreRecordRepository.findClassTotalScoreRanking(classId, null, null, null);
        if (!classRanking.isEmpty()) {
            int classTotal = classRanking.size();
            int classRank = 0;
            for (int i = 0; i < classRanking.size(); i++) {
                Long sid = ((Number) classRanking.get(i)[0]).longValue();
                if (sid.equals(studentId)) {
                    classRank = i + 1;
                    break;
                }
            }
            if (classRank > 0) {
                int threshold = (int) Math.ceil(classTotal * WarningThresholdConfig.RANK_PERCENTILE);
                if (classRank > classTotal - threshold) {
                    String reason = "班级总分排名 " + classRank + "/" + classTotal + "（后 30%），触发普通提醒";
                    warnings.add(new WarningItemDTO(
                            WarningType.RANK_BOTTOM.getLabel(),
                            WarningLevel.NORMAL.getLabel(),
                            null,
                            classRank + "/" + classTotal,
                            "后 30%",
                            reason,
                            Collections.emptyList()
                    ));
                    maxLevel = maxLevel == null ? WarningLevel.NORMAL : WarningLevel.max(maxLevel, WarningLevel.NORMAL);
                }
            }
        }

        List<Object[]> gradeRanking = scoreRecordRepository.findGradeTotalScoreRanking(null, null, null);
        if (!gradeRanking.isEmpty()) {
            int gradeTotal = gradeRanking.size();
            int gradeRank = 0;
            for (int i = 0; i < gradeRanking.size(); i++) {
                Long sid = ((Number) gradeRanking.get(i)[0]).longValue();
                if (sid.equals(studentId)) {
                    gradeRank = i + 1;
                    break;
                }
            }
            if (gradeRank > 0) {
                int threshold = (int) Math.ceil(gradeTotal * WarningThresholdConfig.RANK_PERCENTILE);
                if (gradeRank > gradeTotal - threshold) {
                    String reason = "年级总分排名 " + gradeRank + "/" + gradeTotal + "（后 30%），触发普通提醒";
                    warnings.add(new WarningItemDTO(
                            WarningType.RANK_BOTTOM.getLabel(),
                            WarningLevel.NORMAL.getLabel(),
                            null,
                            gradeRank + "/" + gradeTotal,
                            "后 30%",
                            reason,
                            Collections.emptyList()
                    ));
                    maxLevel = maxLevel == null ? WarningLevel.NORMAL : WarningLevel.max(maxLevel, WarningLevel.NORMAL);
                }
            }
        }

        return new StudentWarningsDTO(
                studentId,
                student.getStudentNo(),
                student.getName(),
                student.getClassInfo().getClassName(),
                maxLevel != null ? maxLevel.getLabel() : null,
                failCount,
                warnings
        );
    }

    public List<StudentWarningsDTO> calculateClassWarnings(Long classId) {
        List<Student> students = studentRepository.findByClassId(classId);
        if (students.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> studentIds = students.stream().map(Student::getId).collect(Collectors.toList());
        List<ScoreRecord> allRecords = scoreRecordRepository.findByStudentIdInAndExamType(studentIds, "期末");

        Map<Long, List<ScoreRecord>> recordsByStudent = allRecords.stream()
                .collect(Collectors.groupingBy(ScoreRecord::getStudentId));

        List<StudentWarningsDTO> result = new ArrayList<>();
        for (Student student : students) {
            List<ScoreRecord> records = recordsByStudent.getOrDefault(student.getId(), Collections.emptyList());
            result.add(buildStudentWarnings(student, records, records));
        }
        return result;
    }

    public List<StudentWarningsDTO> calculateAllWarnings(Long grade, Long classId, String level, String type) {
        List<Student> students;
        if (classId != null) {
            students = studentRepository.findByClassId(classId);
        } else if (grade != null) {
            students = studentRepository.findByGrade(grade);
        } else {
            students = studentRepository.findAll();
        }

        if (students.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> studentIds = students.stream().map(Student::getId).collect(Collectors.toList());
        List<ScoreRecord> allRecords = scoreRecordRepository.findByStudentIdInAndExamType(studentIds, "期末");

        Map<Long, List<ScoreRecord>> recordsByStudent = allRecords.stream()
                .collect(Collectors.groupingBy(ScoreRecord::getStudentId));

        List<StudentWarningsDTO> result = new ArrayList<>();
        for (Student student : students) {
            List<ScoreRecord> records = recordsByStudent.getOrDefault(student.getId(), Collections.emptyList());
            StudentWarningsDTO sw = buildStudentWarnings(student, records, records);
            if (sw.getMaxLevel() == null) continue;

            if (level != null && !level.isEmpty() && !level.equals(sw.getMaxLevel())) continue;

            if (type != null && !type.isEmpty()) {
                boolean hasType = sw.getWarnings().stream()
                        .anyMatch(w -> w.getType().equals(type));
                if (!hasType) continue;
            }

            result.add(sw);
        }
        return result;
    }
}
