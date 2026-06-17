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

    public List<CourseOptionDTO> getCourseOptions(String academicYear, String semester) {
        List<Course> courses = courseRepository.findAll();

        // 按学期过滤
        List<Course> filtered = courses.stream()
                .filter(c -> academicYear == null || academicYear.isEmpty() || academicYear.equals(c.getAcademicYear()))
                .filter(c -> semester == null || semester.isEmpty() || semester.equals(c.getSemester()))
                .toList();

        // 按 courseName 去重，保留第一个出现的
        Map<String, Course> uniqueCourses = new LinkedHashMap<>();
        for (Course c : filtered) {
            uniqueCourses.putIfAbsent(c.getCourseName(), c);
        }

        return uniqueCourses.values().stream()
                .map(c -> new CourseOptionDTO(c.getId(), c.getCourseName()))
                .toList();
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

        // 获取课程信息，判断是否为语文数英
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        boolean isMajorCourse = courseOpt
                .map(c -> isMajorCourse(c.getCourseName()))
                .orElse(false);

        if (isMajorCourse) {
            return getMajorCourseDistribution(courseId);
        } else {
            return getOtherCourseDistribution(courseId);
        }
    }

    private boolean isMajorCourse(String courseName) {
        return courseName.contains("语文") || courseName.contains("数学") || courseName.contains("英语");
    }

    private ScoreDistributionDTO getMajorCourseDistribution(Long courseId) {
        String[] allLabels = {"50-59", "60-69", "70-79", "80-89", "90-99", "100-109", "110-119", "120-129", "130-140"};
        Map<String, Long> map = new LinkedHashMap<>();
        for (String label : allLabels) {
            map.put(label, 0L);
        }

        List<ScoreRecord> records = scoreRecordRepository.findByCourseId(courseId);
        for (ScoreRecord record : records) {
            if (record.getExamType() == null || !record.getExamType().equals("期末")) continue;
            if (record.getIsDeleted() != null && record.getIsDeleted() == 1) continue;

            int score = record.getScoreValue().intValue();
            String label = getMajorCourseLabel(score);
            if (label != null) {
                map.merge(label, 1L, Long::sum);
            }
        }

        return new ScoreDistributionDTO(
                new ArrayList<>(map.keySet()),
                new ArrayList<>(map.values())
        );
    }

    private String getMajorCourseLabel(int score) {
        if (score < 60) return "50-59";
        if (score < 70) return "60-69";
        if (score < 80) return "70-79";
        if (score < 90) return "80-89";
        if (score < 100) return "90-99";
        if (score < 110) return "100-109";
        if (score < 120) return "110-119";
        if (score < 130) return "120-129";
        return "130-140";
    }

    private ScoreDistributionDTO getOtherCourseDistribution(Long courseId) {
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

    public ScoreTrendDTO getScoreTrend(Long studentId, String academicYear, String semester, String examType) {
        List<ScoreRecord> records = scoreRecordRepository.findByStudentIdWithFilters(studentId, academicYear, semester, examType);

        List<String> labels = new ArrayList<>();
        List<BigDecimal> studentScores = new ArrayList<>();
        List<BigDecimal> classAverages = new ArrayList<>();
        List<BigDecimal> gradeAverages = new ArrayList<>();

        // 按课程ID分组
        Map<Long, List<ScoreRecord>> byCourse = new LinkedHashMap<>();
        for (ScoreRecord r : records) {
            byCourse.computeIfAbsent(r.getCourseId(), k -> new ArrayList<>()).add(r);
        }

        // 对每门课程计算平均
        for (Map.Entry<Long, List<ScoreRecord>> entry : byCourse.entrySet()) {
            List<ScoreRecord> courseRecords = entry.getValue();
            Course course = courseRecords.get(0).getCourse();

            // 标签不含考试类型（因为已合并）
            String label = course.getAcademicYear() + " " + course.getSemester() + " " + course.getCourseName();
            labels.add(label);

            // 学生平均分
            BigDecimal studentAvg = courseRecords.stream()
                    .map(ScoreRecord::getScoreValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(courseRecords.size()), 1, RoundingMode.HALF_UP);
            studentScores.add(studentAvg);

            // 班级平均和年级平均
            BigDecimal classAvgSum = BigDecimal.ZERO;
            BigDecimal gradeAvgSum = BigDecimal.ZERO;
            int count = 0;

            for (ScoreRecord r : courseRecords) {
                List<Object[]> avgList = scoreRecordRepository.findCourseAverages(studentId, r.getCourseId(), r.getExamType());
                if (!avgList.isEmpty()) {
                    Object[] avgs = avgList.get(0);
                    if (avgs[0] != null) classAvgSum = classAvgSum.add((BigDecimal) avgs[0]);
                    if (avgs[1] != null) gradeAvgSum = gradeAvgSum.add((BigDecimal) avgs[1]);
                    count++;
                }
            }

            BigDecimal classAvg = count > 0 ?
                    classAvgSum.divide(BigDecimal.valueOf(count), 1, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            BigDecimal gradeAvg = count > 0 ?
                    gradeAvgSum.divide(BigDecimal.valueOf(count), 1, RoundingMode.HALF_UP) : BigDecimal.ZERO;

            classAverages.add(classAvg);
            gradeAverages.add(gradeAvg);
        }

        return new ScoreTrendDTO(labels, studentScores, classAverages, gradeAverages);
    }

    private static final Set<String> CORE_COURSES = Set.of(
            "语文", "数学", "英语", "物理", "化学", "生物", "历史", "地理", "政治"
    );

    public RadarChartDTO getRadarData(Long studentId, String academicYear, String semester, String examType) {
        List<ScoreRecord> records = scoreRecordRepository.findByStudentAndFilters(studentId, academicYear, semester, examType);

        List<String> courseNames = new ArrayList<>();
        List<BigDecimal> studentScores = new ArrayList<>();
        List<BigDecimal> classAverages = new ArrayList<>();
        List<BigDecimal> gradeAverages = new ArrayList<>();

        boolean isAllSemesters = (academicYear == null || academicYear.isEmpty());

        // 全部模式按 courseName 分组，单学期按 courseId 分组
        Map<String, List<ScoreRecord>> grouped = new LinkedHashMap<>();
        for (ScoreRecord r : records) {
            String key = isAllSemesters ? r.getCourse().getCourseName() : String.valueOf(r.getCourseId());
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(r);
        }

        for (Map.Entry<String, List<ScoreRecord>> entry : grouped.entrySet()) {
            List<ScoreRecord> courseRecords = entry.getValue();
            Course course = courseRecords.get(0).getCourse();

            // 全部模式下跳过非核心科目
            if (isAllSemesters && !CORE_COURSES.contains(course.getCourseName())) {
                continue;
            }

            // 学生平均分 = 所有考试类型的平均
            BigDecimal studentAvg = courseRecords.stream()
                    .map(ScoreRecord::getScoreValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(courseRecords.size()), 1, RoundingMode.HALF_UP);

            // 班级平均和年级平均
            BigDecimal classAvgSum = BigDecimal.ZERO;
            BigDecimal gradeAvgSum = BigDecimal.ZERO;
            int count = 0;

            for (ScoreRecord r : courseRecords) {
                List<Object[]> avgList = scoreRecordRepository.findCourseAverages(studentId, r.getCourseId(), r.getExamType());
                if (!avgList.isEmpty()) {
                    Object[] avgs = avgList.get(0);
                    if (avgs[0] != null) classAvgSum = classAvgSum.add((BigDecimal) avgs[0]);
                    if (avgs[1] != null) gradeAvgSum = gradeAvgSum.add((BigDecimal) avgs[1]);
                    count++;
                }
            }

            BigDecimal classAvg = count > 0 ?
                    classAvgSum.divide(BigDecimal.valueOf(count), 1, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            BigDecimal gradeAvg = count > 0 ?
                    gradeAvgSum.divide(BigDecimal.valueOf(count), 1, RoundingMode.HALF_UP) : BigDecimal.ZERO;

            courseNames.add(course.getCourseName());
            studentScores.add(studentAvg);
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

    // ========== 成绩趋势对比 ==========

    public ScoreTrendDetailDTO getScoreTrendDetail(Long studentId, String academicYear, String semester, String examType) {
        List<ScoreRecord> allRecords = scoreRecordRepository.findByStudentIdOrderedWithFilters(studentId, academicYear, semester, examType);

        // 按课程分组
        Map<String, List<ScoreRecord>> byCourse = new LinkedHashMap<>();
        for (ScoreRecord r : allRecords) {
            String courseName = r.getCourse().getCourseName();
            byCourse.computeIfAbsent(courseName, k -> new ArrayList<>()).add(r);
        }

        // 构建各科趋势
        List<ScoreTrendDetailDTO.CourseTrendItem> courseTrends = new ArrayList<>();
        for (Map.Entry<String, List<ScoreRecord>> entry : byCourse.entrySet()) {
            String courseName = entry.getKey();
            List<ScoreRecord> records = entry.getValue();

            List<ScoreTrendDetailDTO.ExamScore> exams = new ArrayList<>();
            for (ScoreRecord r : records) {
                Course c = r.getCourse();
                String label = c.getAcademicYear() + " " + c.getSemester() + " " + r.getExamType();

                List<Object[]> avgList = scoreRecordRepository.findCourseAverages(studentId, r.getCourseId(), r.getExamType());
                Object[] avgs = avgList.isEmpty() ? new Object[]{null, null} : avgList.get(0);
                BigDecimal classAvg = avgs[0] != null ? ((BigDecimal) avgs[0]).setScale(1, RoundingMode.HALF_UP) : BigDecimal.ZERO;
                BigDecimal gradeAvg = avgs[1] != null ? ((BigDecimal) avgs[1]).setScale(1, RoundingMode.HALF_UP) : BigDecimal.ZERO;

                exams.add(new ScoreTrendDetailDTO.ExamScore(
                        label, c.getAcademicYear(), c.getSemester(), r.getExamType(),
                        r.getScoreValue(), classAvg, gradeAvg));
            }

            BigDecimal latestChange = BigDecimal.ZERO;
            String trendDirection = "持平";
            if (exams.size() >= 2) {
                BigDecimal latest = exams.get(exams.size() - 1).getScore();
                BigDecimal previous = exams.get(exams.size() - 2).getScore();
                latestChange = latest.subtract(previous).setScale(1, RoundingMode.HALF_UP);
                if (latestChange.compareTo(BigDecimal.ZERO) > 0) {
                    trendDirection = "上升";
                } else if (latestChange.compareTo(BigDecimal.ZERO) < 0) {
                    trendDirection = "下降";
                }
            }

            courseTrends.add(new ScoreTrendDetailDTO.CourseTrendItem(
                    courseName, exams, latestChange, trendDirection));
        }

        // 构建学期汇总
        Map<String, List<ScoreRecord>> bySemester = new LinkedHashMap<>();
        for (ScoreRecord r : allRecords) {
            Course c = r.getCourse();
            String key = c.getAcademicYear() + " " + c.getSemester() + " " + r.getExamType();
            bySemester.computeIfAbsent(key, k -> new ArrayList<>()).add(r);
        }

        List<ScoreTrendDetailDTO.SemesterSummary> semesterSummaries = new ArrayList<>();
        for (Map.Entry<String, List<ScoreRecord>> entry : bySemester.entrySet()) {
            BigDecimal total = BigDecimal.ZERO;
            for (ScoreRecord r : entry.getValue()) {
                total = total.add(r.getScoreValue());
            }
            BigDecimal avg = entry.getValue().size() > 0
                    ? total.divide(BigDecimal.valueOf(entry.getValue().size()), 1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            // 计算班级和年级平均总分
            BigDecimal classAvgTotal = BigDecimal.ZERO;
            BigDecimal gradeAvgTotal = BigDecimal.ZERO;
            int count = 0;
            for (ScoreRecord r : entry.getValue()) {
                List<Object[]> avgs = scoreRecordRepository.findCourseAverages(studentId, r.getCourseId(), r.getExamType());
                if (!avgs.isEmpty()) {
                    Object[] avgArr = avgs.get(0);
                    if (avgArr[0] != null) classAvgTotal = classAvgTotal.add((BigDecimal) avgArr[0]);
                    if (avgArr[1] != null) gradeAvgTotal = gradeAvgTotal.add((BigDecimal) avgArr[1]);
                    count++;
                }
            }
            if (count > 0) {
                classAvgTotal = classAvgTotal.divide(BigDecimal.valueOf(count), 1, RoundingMode.HALF_UP);
                gradeAvgTotal = gradeAvgTotal.divide(BigDecimal.valueOf(count), 1, RoundingMode.HALF_UP);
            }

            semesterSummaries.add(new ScoreTrendDetailDTO.SemesterSummary(
                    entry.getKey(), total, classAvgTotal, gradeAvgTotal, entry.getValue().size()));
        }

        return new ScoreTrendDetailDTO(courseTrends, semesterSummaries);
    }
}
