package com.test.server.service;

import com.test.server.dto.*;
import com.test.server.entity.Course;
import com.test.server.repository.CourseRepository;
import com.test.server.repository.ScoreRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CourseRepository courseRepository;
    private final ScoreRecordRepository scoreRecordRepository;

    public List<CourseOptionDTO> getCourseOptions() {
        List<Course> courses = courseRepository.findAll();
        List<CourseOptionDTO> options = new ArrayList<>();
        for (Course c : courses) {
            options.add(new CourseOptionDTO(c.getId(), c.getCourseName()));
        }
        return options;
    }

    public List<SemesterOptionDTO> getSemesterOptions() {
        List<Object[]> rows = scoreRecordRepository.findDistinctYearSemesters();
        List<SemesterOptionDTO> options = new ArrayList<>();
        for (Object[] row : rows) {
            String year = (String) row[0];
            String sem = (String) row[1];
            options.add(new SemesterOptionDTO(year, sem, year + "学年 " + sem));
        }
        return options;
    }

    public DashboardStatsDTO getStats(String academicYear, String semester) {
        List<Long> courseIds = filterCourseIds(academicYear, semester);

        long courseCount = courseIds.size();
        long studentCount = scoreRecordRepository.countDistinctStudentsByCourseIds(courseIds);
        BigDecimal avgScore = scoreRecordRepository.findAverageScoreByCourseIds(courseIds);
        long failingCount = scoreRecordRepository.countFailingStudentsByCourseIds(courseIds);
        BigDecimal maxScore = scoreRecordRepository.findMaxScoreByCourseIds(courseIds);
        long passingCount = scoreRecordRepository.countPassingStudentsByCourseIds(courseIds);

        BigDecimal passRate = BigDecimal.ZERO;
        if (studentCount > 0) {
            passRate = BigDecimal.valueOf(passingCount)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(studentCount), 1, RoundingMode.HALF_UP);
        }

        return new DashboardStatsDTO(
                courseCount,
                studentCount,
                avgScore != null ? avgScore.setScale(1, RoundingMode.HALF_UP) : BigDecimal.ZERO,
                failingCount,
                maxScore != null ? maxScore : BigDecimal.ZERO,
                passRate
        );
    }

    public ScoreDistributionDTO getScoreDistribution(Long courseId) {
        if (courseId == null) {
            return new ScoreDistributionDTO(
                    Arrays.asList("0-59", "60-69", "70-79", "80-89", "90-100"),
                    Arrays.asList(0L, 0L, 0L, 0L, 0L)
            );
        }

        List<Object[]> rows = scoreRecordRepository.findScoreDistributionByCourseId(courseId);
        String[] allLabels = {"0-59", "60-69", "70-79", "80-89", "90-100"};
        Map<String, Long> map = new LinkedHashMap<>();
        for (String label : allLabels) {
            map.put(label, 0L);
        }
        for (Object[] row : rows) {
            String label = (String) row[0];
            Long count = ((Number) row[1]).longValue();
            map.put(label, count);
        }

        return new ScoreDistributionDTO(
                new ArrayList<>(map.keySet()),
                new ArrayList<>(map.values())
        );
    }

    public List<TopStudentDTO> getTop5BySubject(Long courseId) {
        if (courseId == null) {
            return Collections.emptyList();
        }

        List<Object[]> rows = scoreRecordRepository.findTop5ByCourseId(courseId);
        List<TopStudentDTO> result = new ArrayList<>();
        int rank = 1;
        for (Object[] row : rows) {
            String name = (String) row[0];
            String className = (String) row[1];
            BigDecimal score = (BigDecimal) row[2];
            result.add(new TopStudentDTO(rank++, name, className, score));
        }
        return result;
    }

    public List<TopStudentDTO> getTop5Total(String academicYear, String semester) {
        List<Long> courseIds = filterCourseIds(academicYear, semester);
        if (courseIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Object[]> rows = scoreRecordRepository.findTop5TotalByCourseIds(courseIds);
        List<TopStudentDTO> result = new ArrayList<>();
        int rank = 1;
        for (Object[] row : rows) {
            String name = (String) row[0];
            String className = (String) row[1];
            BigDecimal totalScore = (BigDecimal) row[2];
            result.add(new TopStudentDTO(rank++, name, className, totalScore));
        }
        return result;
    }

    public List<WarningDTO> getWarnings(String academicYear, String semester) {
        List<Long> courseIds = filterCourseIds(academicYear, semester);
        if (courseIds.isEmpty()) {
            return Arrays.asList(
                    new WarningDTO("不及格", 0),
                    new WarningDTO("低分预警", 0)
            );
        }

        List<Object[]> rows = scoreRecordRepository.findWarningsByCourseIds(courseIds);
        List<WarningDTO> result = new ArrayList<>();
        for (Object[] row : rows) {
            String type = (String) row[0];
            long count = ((Number) row[1]).longValue();
            result.add(new WarningDTO(type, count));
        }
        return result;
    }

    public List<RecentRecordDTO> getRecentRecords(int limit) {
        List<Course> allCourses = courseRepository.findAll();
        List<Long> courseIds = allCourses.stream().map(Course::getId).toList();
        if (courseIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Object[]> rows = scoreRecordRepository.findRecentRecordsByCourseIds(courseIds, limit);
        List<RecentRecordDTO> result = new ArrayList<>();
        for (Object[] row : rows) {
            Long id = ((Number) row[0]).longValue();
            String studentName = (String) row[1];
            String courseName = (String) row[2];
            BigDecimal scoreValue = (BigDecimal) row[3];
            String examType = (String) row[4];
            LocalDateTime createTime = ((Timestamp) row[5]).toLocalDateTime();
            result.add(new RecentRecordDTO(id, studentName, courseName, scoreValue, examType, createTime));
        }
        return result;
    }

    private List<Long> filterCourseIds(String academicYear, String semester) {
        List<Course> courses = courseRepository.findAll();
        return courses.stream()
                .filter(c -> academicYear == null || academicYear.isEmpty() || academicYear.equals(c.getAcademicYear()))
                .filter(c -> semester == null || semester.isEmpty() || semester.equals(c.getSemester()))
                .map(Course::getId)
                .toList();
    }
}
