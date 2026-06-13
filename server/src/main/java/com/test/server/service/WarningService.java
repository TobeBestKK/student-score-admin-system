package com.test.server.service;

import com.test.server.config.WarningThresholdConfig;
import com.test.server.config.WarningThresholdConfig.WarningLevel;
import com.test.server.config.WarningThresholdConfig.WarningType;
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
        List<WarningItemDTO> warnings = new ArrayList<>();
        WarningLevel maxLevel = null;

        // 1. 单科预警
        for (ScoreRecord record : records) {
            BigDecimal score = record.getScoreValue();
            String courseName = record.getCourse().getCourseName();

            if (score.doubleValue() < WarningThresholdConfig.FAIL_SCORE) {
                // 重点关注: 单科 < 60
                String reason = "期末《" + courseName + "》" + score.intValue() + " 分，触发重点关注";
                warnings.add(new WarningItemDTO(
                        WarningType.LOW_SCORE.getLabel(),
                        WarningLevel.MODERATE.getLabel(),
                        courseName,
                        String.valueOf(score.intValue()),
                        "< 60",
                        reason
                ));
                maxLevel = maxLevel == null ? WarningLevel.MODERATE : WarningLevel.max(maxLevel, WarningLevel.MODERATE);
            } else if (score.doubleValue() <= WarningThresholdConfig.LOW_SCORE_MAX) {
                // 普通提醒: 单科 60-65
                String reason = "期末《" + courseName + "》" + score.intValue() + " 分，触发普通提醒";
                warnings.add(new WarningItemDTO(
                        WarningType.LOW_SCORE.getLabel(),
                        WarningLevel.NORMAL.getLabel(),
                        courseName,
                        String.valueOf(score.intValue()),
                        "60-65",
                        reason
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
                    reason
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
                    reason
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
                        reason
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
                            reason
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
                            reason
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
        List<StudentWarningsDTO> result = new ArrayList<>();
        for (Student student : students) {
            result.add(calculateStudentWarnings(student.getId()));
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

        List<StudentWarningsDTO> result = new ArrayList<>();
        for (Student student : students) {
            StudentWarningsDTO sw = calculateStudentWarnings(student.getId());
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
