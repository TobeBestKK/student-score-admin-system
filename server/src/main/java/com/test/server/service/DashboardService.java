package com.test.server.service;

import com.test.server.dto.*;
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
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CourseRepository courseRepository;
    private final ScoreRecordRepository scoreRecordRepository;
    private final StudentRepository studentRepository;

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

    public DashboardStatsDTO getStats(String academicYear, String semester, Long teacherId) {
        List<Long> courseIds = filterCourseIds(academicYear, semester, teacherId);

        long courseCount = courseIds.size();
        long studentCount = scoreRecordRepository.countDistinctStudentsByCourseIds(courseIds);
        BigDecimal avgScore = scoreRecordRepository.findAverageScoreByCourseIds(courseIds);
        long failingCount = scoreRecordRepository.countFailingStudentsByCourseIds(courseIds);
        BigDecimal maxScore = scoreRecordRepository.findMaxTotalScoreByCourseIds(courseIds);
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
                    Arrays.asList("0-9", "10-19", "20-29", "30-39", "40-49", "50-59", "60-69", "70-79", "80-89", "90-100"),
                    Arrays.asList(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L)
            );
        }

        List<Object[]> rows = scoreRecordRepository.findScoreDistributionByCourseId(courseId);
        String[] allLabels = {"0-9", "10-19", "20-29", "30-39", "40-49", "50-59", "60-69", "70-79", "80-89", "90-100"};
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
        List<Long> courseIds = filterCourseIds(academicYear, semester, null);
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
        List<Long> courseIds = filterCourseIds(academicYear, semester, null);
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

        List<Object[]> rows = scoreRecordRepository.findRecentRecordsByCourseIds(courseIds);
        List<RecentRecordDTO> result = new ArrayList<>();
        int count = 0;
        for (Object[] row : rows) {
            if (count >= limit) {
                break;
            }
            Long id = ((Number) row[0]).longValue();
            String studentName = (String) row[1];
            String courseName = (String) row[2];
            BigDecimal scoreValue = (BigDecimal) row[3];
            String examType = (String) row[4];
            LocalDateTime createTime = row[5] instanceof Timestamp
                    ? ((Timestamp) row[5]).toLocalDateTime()
                    : (LocalDateTime) row[5];
            result.add(new RecentRecordDTO(id, studentName, courseName, scoreValue, examType, createTime));
            count++;
        }
        return result;
    }

    private List<Long> filterCourseIds(String academicYear, String semester, Long teacherId) {
        List<Course> courses = courseRepository.findAll();
        return courses.stream()
                .filter(c -> teacherId == null || teacherId.equals(c.getTeacherId()))
                .filter(c -> academicYear == null || academicYear.isEmpty() || academicYear.equals(c.getAcademicYear()))
                .filter(c -> semester == null || semester.isEmpty() || semester.equals(c.getSemester()))
                .map(Course::getId)
                .toList();
    }

    // ========== 学生端方法 ==========

    public StudentProfileDTO getStudentProfile(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("学生不存在"));
        return new StudentProfileDTO(
                student.getId(),
                student.getName(),
                student.getStudentNo(),
                student.getClassInfo().getClassName(),
                student.getClassInfo().getMajor(),
                student.getClassInfo().getGrade(),
                student.getEnrollmentYear()
        );
    }

    public List<StudentCourseScoreDTO> getStudentScores(Long studentId, String academicYear, String semester, String examType) {
        List<ScoreRecord> records = scoreRecordRepository.findByStudentAndFilters(studentId, academicYear, semester, examType);
        List<StudentCourseScoreDTO> result = new ArrayList<>();

        for (ScoreRecord record : records) {
            Long courseId = record.getCourseId();
            String type = record.getExamType();

            int classRank = scoreRecordRepository.countClassRank(studentId, courseId, type);
            int classTotal = scoreRecordRepository.countClassTotal(studentId, courseId, type);
            int gradeRank = scoreRecordRepository.countGradeRank(studentId, courseId, type);
            int gradeTotal = scoreRecordRepository.countGradeTotal(courseId, type);

            result.add(new StudentCourseScoreDTO(
                    record.getId(),
                    courseId,
                    record.getCourse().getCourseName(),
                    record.getScoreValue(),
                    type,
                    record.getCourse().getCredit(),
                    classRank,
                    classTotal,
                    gradeRank,
                    gradeTotal
            ));
        }
        return result;
    }

    public StudentStatsDTO getStudentStats(Long studentId, String academicYear, String semester, String examType) {
        List<ScoreRecord> records = scoreRecordRepository.findByStudentAndFilters(studentId, academicYear, semester, examType);

        BigDecimal totalScore = BigDecimal.ZERO;
        BigDecimal earnedCredit = BigDecimal.ZERO;
        int failCount = 0;
        int count = records.size();

        for (ScoreRecord record : records) {
            BigDecimal score = record.getScoreValue();
            totalScore = totalScore.add(score);

            if (score.compareTo(BigDecimal.valueOf(60)) >= 0) {
                earnedCredit = earnedCredit.add(record.getCourse().getCredit());
            } else {
                failCount++;
            }
        }

        BigDecimal averageScore = count > 0
                ? totalScore.divide(BigDecimal.valueOf(count), 1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // 计算班级总分排名
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("学生不存在"));
        Long classId = student.getClassId();

        List<Object[]> classRanking = scoreRecordRepository.findClassTotalScoreRanking(classId, academicYear, semester, examType);
        int classRank = 0;
        int classTotal = classRanking.size();
        for (int i = 0; i < classRanking.size(); i++) {
            Long sid = ((Number) classRanking.get(i)[0]).longValue();
            if (sid.equals(studentId)) {
                classRank = i + 1;
                break;
            }
        }

        // 计算年级总分排名
        List<Object[]> gradeRanking = scoreRecordRepository.findGradeTotalScoreRanking(academicYear, semester, examType);
        int gradeRank = 0;
        int gradeTotal = gradeRanking.size();
        for (int i = 0; i < gradeRanking.size(); i++) {
            Long sid = ((Number) gradeRanking.get(i)[0]).longValue();
            if (sid.equals(studentId)) {
                gradeRank = i + 1;
                break;
            }
        }

        // 计算总学分（动态）
        BigDecimal totalCredit = courseRepository.sumTotalCreditByFilters(academicYear, semester);

        // 计算学分预警（阈值 60%）
        BigDecimal creditThreshold = totalCredit.multiply(BigDecimal.valueOf(0.6));
        boolean creditWarning = earnedCredit.compareTo(creditThreshold) < 0;

        return new StudentStatsDTO(
                totalScore.setScale(1, RoundingMode.HALF_UP),
                averageScore,
                earnedCredit.setScale(1, RoundingMode.HALF_UP),
                failCount,
                classRank,
                classTotal,
                gradeRank,
                gradeTotal,
                totalCredit.setScale(1, RoundingMode.HALF_UP),
                creditWarning
        );
    }

    public ScoreTrendDTO getScoreTrend(Long studentId) {
        List<ScoreRecord> records = scoreRecordRepository.findByStudentIdAllSemesters(studentId);

        List<String> labels = new ArrayList<>();
        List<BigDecimal> studentScores = new ArrayList<>();
        List<BigDecimal> classAverages = new ArrayList<>();
        List<BigDecimal> gradeAverages = new ArrayList<>();

        for (ScoreRecord record : records) {
            Course course = record.getCourse();
            String label = course.getAcademicYear() + " " + course.getSemester() + " " + record.getExamType() + " " + course.getCourseName();
            labels.add(label);
            studentScores.add(record.getScoreValue());

            List<Object[]> avgList = scoreRecordRepository.findCourseAverages(studentId, record.getCourseId(), record.getExamType());
            Object[] avgs = avgList.isEmpty() ? new Object[]{null, null} : avgList.get(0);
            BigDecimal classAvg = avgs[0] != null ? ((BigDecimal) avgs[0]).setScale(1, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            BigDecimal gradeAvg = avgs[1] != null ? ((BigDecimal) avgs[1]).setScale(1, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            classAverages.add(classAvg);
            gradeAverages.add(gradeAvg);
        }

        return new ScoreTrendDTO(labels, studentScores, classAverages, gradeAverages);
    }

    public RadarChartDTO getRadarData(Long studentId, String academicYear, String semester, String examType) {
        List<ScoreRecord> records = scoreRecordRepository.findByStudentAndFilters(studentId, academicYear, semester, examType);

        List<String> courseNames = new ArrayList<>();
        List<BigDecimal> studentScores = new ArrayList<>();
        List<BigDecimal> classAverages = new ArrayList<>();
        List<BigDecimal> gradeAverages = new ArrayList<>();

        for (ScoreRecord record : records) {
            courseNames.add(record.getCourse().getCourseName());
            studentScores.add(record.getScoreValue());

            List<Object[]> avgList = scoreRecordRepository.findCourseAverages(studentId, record.getCourseId(), record.getExamType());
            Object[] avgs = avgList.isEmpty() ? new Object[]{null, null} : avgList.get(0);
            BigDecimal classAvg = avgs[0] != null ? ((BigDecimal) avgs[0]).setScale(1, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            BigDecimal gradeAvg = avgs[1] != null ? ((BigDecimal) avgs[1]).setScale(1, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            classAverages.add(classAvg);
            gradeAverages.add(gradeAvg);
        }

        return new RadarChartDTO(courseNames, studentScores, classAverages, gradeAverages);
    }

    // ========== 年级排名查询 ==========

    public List<GradeTotalRankDTO> getGradeTotalRanking(String academicYear, String semester, String examType) {
        List<Object[]> rows = scoreRecordRepository.findGradeTotalRanking(academicYear, semester, examType);
        List<GradeTotalRankDTO> result = new ArrayList<>();

        int rank = 1;
        for (Object[] row : rows) {
            Long studentId = ((Number) row[0]).longValue();
            String name = (String) row[1];
            String className = (String) row[2];
            BigDecimal totalScore = (BigDecimal) row[3];
            BigDecimal avgScore = row[4] != null ? (BigDecimal) row[4] : BigDecimal.ZERO;

            // 获取该学生的各科成绩
            List<CourseScoreDTO> courses = getStudentCourses(studentId, academicYear, semester, examType);

            result.add(new GradeTotalRankDTO(studentId, name, className, totalScore, avgScore, courses));
            rank++;
        }

        return result;
    }

    private List<CourseScoreDTO> getStudentCourses(Long studentId, String academicYear, String semester, String examType) {
        List<ScoreRecord> records = scoreRecordRepository.findByStudentAndFilters(studentId, academicYear, semester, examType);
        List<CourseScoreDTO> courses = new ArrayList<>();

        for (ScoreRecord record : records) {
            courses.add(new CourseScoreDTO(record.getCourse().getCourseName(), record.getScoreValue()));
        }

        return courses;
    }

    public List<CourseRankDTO> getCourseRanking(String courseName, String academicYear, String semester, String examType) {
        List<Object[]> rows = scoreRecordRepository.findCourseRanking(courseName, academicYear, semester, examType);
        List<CourseRankDTO> result = new ArrayList<>();

        for (Object[] row : rows) {
            Long studentId = ((Number) row[0]).longValue();
            String name = (String) row[1];
            String className = (String) row[2];
            BigDecimal score = (BigDecimal) row[3];

            result.add(new CourseRankDTO(studentId, name, className, score));
        }

        return result;
    }
}
