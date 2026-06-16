package com.test.server.repository;

import com.test.server.entity.ScoreRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ScoreRecordRepository extends JpaRepository<ScoreRecord, Long> {

    List<ScoreRecord> findByCourseId(Long courseId);

    List<ScoreRecord> findByStudentId(Long studentId);

    @Query("SELECT COUNT(DISTINCT sr.studentId) FROM ScoreRecord sr WHERE sr.courseId IN :courseIds")
    long countDistinctStudentsByCourseIds(@Param("courseIds") List<Long> courseIds);

    @Query("SELECT AVG(sr.scoreValue) FROM ScoreRecord sr WHERE sr.courseId IN :courseIds AND sr.examType = '期末'")
    BigDecimal findAverageScoreByCourseIds(@Param("courseIds") List<Long> courseIds);

    @Query("SELECT COUNT(DISTINCT sr.studentId) FROM ScoreRecord sr WHERE sr.courseId IN :courseIds AND sr.examType = '期末' AND sr.scoreValue < 60")
    long countFailingStudentsByCourseIds(@Param("courseIds") List<Long> courseIds);

    @Query(value = "SELECT SUM(sr.score_value) FROM score_record sr " +
            "WHERE sr.course_id IN :courseIds AND sr.exam_type = '期末' AND sr.is_deleted = 0 " +
            "GROUP BY sr.student_id " +
            "ORDER BY SUM(sr.score_value) DESC LIMIT 1",
            nativeQuery = true)
    BigDecimal findMaxTotalScoreByCourseIds(@Param("courseIds") List<Long> courseIds);

    @Query("SELECT COUNT(DISTINCT sr.studentId) FROM ScoreRecord sr WHERE sr.courseId IN :courseIds AND sr.examType = '期末' AND sr.scoreValue >= 60")
    long countPassingStudentsByCourseIds(@Param("courseIds") List<Long> courseIds);

    @Query(value = "SELECT CASE " +
            "WHEN score_value < 10 THEN '0-9' " +
            "WHEN score_value < 20 THEN '10-19' " +
            "WHEN score_value < 30 THEN '20-29' " +
            "WHEN score_value < 40 THEN '30-39' " +
            "WHEN score_value < 50 THEN '40-49' " +
            "WHEN score_value < 60 THEN '50-59' " +
            "WHEN score_value < 70 THEN '60-69' " +
            "WHEN score_value < 80 THEN '70-79' " +
            "WHEN score_value < 90 THEN '80-89' " +
            "ELSE '90-100' END AS range_label, " +
            "COUNT(*) AS cnt " +
            "FROM score_record " +
            "WHERE course_id = :courseId AND exam_type = '期末' AND is_deleted = 0 " +
            "GROUP BY range_label " +
            "ORDER BY range_label",
            nativeQuery = true)
    List<Object[]> findScoreDistributionByCourseId(@Param("courseId") Long courseId);

    @Query(value = "SELECT s.name AS student_name, c.class_name AS class_name, sr.score_value AS score " +
            "FROM score_record sr " +
            "JOIN student s ON sr.student_id = s.id " +
            "JOIN class_info c ON s.class_id = c.id " +
            "WHERE sr.course_id = :courseId AND sr.exam_type = '期末' AND sr.is_deleted = 0 " +
            "ORDER BY sr.score_value DESC " +
            "LIMIT 5",
            nativeQuery = true)
    List<Object[]> findTop5ByCourseId(@Param("courseId") Long courseId);

    @Query(value = "SELECT s.name AS student_name, c.class_name AS class_name, SUM(sr.score_value) AS total_score " +
            "FROM score_record sr " +
            "JOIN student s ON sr.student_id = s.id " +
            "JOIN class_info c ON s.class_id = c.id " +
            "WHERE sr.course_id IN :courseIds AND sr.exam_type = '期末' AND sr.is_deleted = 0 " +
            "GROUP BY sr.student_id, s.name, c.class_name " +
            "ORDER BY total_score DESC " +
            "LIMIT 5",
            nativeQuery = true)
    List<Object[]> findTop5TotalByCourseIds(@Param("courseIds") List<Long> courseIds);

    @Query(value = "SELECT '不及格' AS type, COUNT(*) AS cnt FROM score_record " +
            "WHERE course_id IN :courseIds AND exam_type = '期末' AND score_value < 60 AND is_deleted = 0 " +
            "UNION ALL " +
            "SELECT '低分预警', COUNT(*) FROM score_record " +
            "WHERE course_id IN :courseIds AND exam_type = '期末' AND score_value < 40 AND is_deleted = 0",
            nativeQuery = true)
    List<Object[]> findWarningsByCourseIds(@Param("courseIds") List<Long> courseIds);

    @Query(value = "SELECT sr.id, s.name AS student_name, co.course_name, sr.score_value, sr.exam_type, sr.create_time " +
            "FROM score_record sr " +
            "JOIN student s ON sr.student_id = s.id " +
            "JOIN course co ON sr.course_id = co.id " +
            "WHERE sr.course_id IN :courseIds AND sr.is_deleted = 0 " +
            "ORDER BY sr.create_time DESC",
            nativeQuery = true)
    List<Object[]> findRecentRecordsByCourseIds(@Param("courseIds") List<Long> courseIds);

    @Query(value = "SELECT DISTINCT co.academic_year, co.semester " +
            "FROM course co " +
            "WHERE co.is_deleted = 0 AND co.academic_year IS NOT NULL " +
            "ORDER BY co.academic_year DESC, co.semester",
            nativeQuery = true)
    List<Object[]> findDistinctYearSemesters();

    // ========== 学生端查询 ==========

    @Query(value = "SELECT sr.* FROM score_record sr " +
            "JOIN course co ON sr.course_id = co.id " +
            "WHERE sr.student_id = :studentId AND sr.is_deleted = 0 " +
            "AND (COALESCE(:academicYear, '') = '' OR co.academic_year = :academicYear) " +
            "AND (COALESCE(:semester, '') = '' OR co.semester = :semester) " +
            "AND (COALESCE(:examType, '') = '' OR sr.exam_type = :examType) " +
            "ORDER BY co.academic_year DESC, co.semester, co.course_name",
            nativeQuery = true)
    List<ScoreRecord> findByStudentAndFilters(
            @Param("studentId") Long studentId,
            @Param("academicYear") String academicYear,
            @Param("semester") String semester,
            @Param("examType") String examType);

    @Query(value = "SELECT COUNT(*) + 1 FROM score_record sr " +
            "JOIN student s ON sr.student_id = s.id " +
            "WHERE sr.course_id = :courseId AND sr.exam_type = :examType AND sr.is_deleted = 0 " +
            "AND s.class_id = (SELECT class_id FROM student WHERE id = :studentId) " +
            "AND sr.score_value > (SELECT score_value FROM score_record " +
            "WHERE student_id = :studentId AND course_id = :courseId AND exam_type = :examType AND is_deleted = 0)",
            nativeQuery = true)
    int countClassRank(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId,
            @Param("examType") String examType);

    @Query(value = "SELECT COUNT(*) + 1 FROM score_record sr " +
            "WHERE sr.course_id = :courseId AND sr.exam_type = :examType AND sr.is_deleted = 0 " +
            "AND sr.score_value > (SELECT score_value FROM score_record " +
            "WHERE student_id = :studentId AND course_id = :courseId AND exam_type = :examType AND is_deleted = 0)",
            nativeQuery = true)
    int countGradeRank(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId,
            @Param("examType") String examType);

    @Query(value = "SELECT COUNT(*) FROM score_record sr " +
            "JOIN student s ON sr.student_id = s.id " +
            "WHERE sr.course_id = :courseId AND sr.exam_type = :examType AND sr.is_deleted = 0 " +
            "AND s.class_id = (SELECT class_id FROM student WHERE id = :studentId)",
            nativeQuery = true)
    int countClassTotal(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId,
            @Param("examType") String examType);

    @Query(value = "SELECT COUNT(*) FROM score_record sr " +
            "WHERE sr.course_id = :courseId AND sr.exam_type = :examType AND sr.is_deleted = 0",
            nativeQuery = true)
    int countGradeTotal(
            @Param("courseId") Long courseId,
            @Param("examType") String examType);

    @Query(value = "SELECT sr.* FROM score_record sr " +
            "WHERE sr.student_id = :studentId AND sr.is_deleted = 0 " +
            "AND sr.exam_type IN ('期中', '期末') " +
            "ORDER BY sr.create_time DESC",
            nativeQuery = true)
    List<ScoreRecord> findByStudentIdAllSemesters(@Param("studentId") Long studentId);

    @Query(value = "SELECT " +
            "(SELECT AVG(sr2.score_value) FROM score_record sr2 " +
            " JOIN student s2 ON sr2.student_id = s2.id " +
            " WHERE sr2.course_id = :courseId AND sr2.exam_type = :examType AND sr2.is_deleted = 0 " +
            " AND s2.class_id = (SELECT class_id FROM student WHERE id = :studentId)) AS class_avg, " +
            "(SELECT AVG(sr3.score_value) FROM score_record sr3 " +
            " WHERE sr3.course_id = :courseId AND sr3.exam_type = :examType AND sr3.is_deleted = 0) AS grade_avg",
            nativeQuery = true)
    List<Object[]> findCourseAverages(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId,
            @Param("examType") String examType);

    @Query(value = "SELECT student_id, SUM(score_value) AS total FROM score_record sr " +
            "JOIN student s ON sr.student_id = s.id " +
            "JOIN course co ON sr.course_id = co.id " +
            "WHERE s.class_id = :classId " +
            "AND (:examType IS NULL OR :examType = '' OR sr.exam_type = :examType) " +
            "AND (:academicYear IS NULL OR :academicYear = '' OR co.academic_year = :academicYear) " +
            "AND (:semester IS NULL OR :semester = '' OR co.semester = :semester) " +
            "AND sr.is_deleted = 0 " +
            "GROUP BY student_id " +
            "ORDER BY total DESC",
            nativeQuery = true)
    List<Object[]> findClassTotalScoreRanking(
            @Param("classId") Long classId,
            @Param("academicYear") String academicYear,
            @Param("semester") String semester,
            @Param("examType") String examType);

    @Query(value = "SELECT student_id, SUM(score_value) AS total FROM score_record sr " +
            "JOIN course co ON sr.course_id = co.id " +
            "WHERE (:examType IS NULL OR :examType = '' OR sr.exam_type = :examType) " +
            "AND (:academicYear IS NULL OR :academicYear = '' OR co.academic_year = :academicYear) " +
            "AND (:semester IS NULL OR :semester = '' OR co.semester = :semester) " +
            "AND sr.is_deleted = 0 " +
            "GROUP BY student_id " +
            "ORDER BY total DESC",
            nativeQuery = true)
    List<Object[]> findGradeTotalScoreRanking(
            @Param("academicYear") String academicYear,
            @Param("semester") String semester,
            @Param("examType") String examType);

    @Query(value = "SELECT sr.* FROM score_record sr " +
            "JOIN course co ON sr.course_id = co.id " +
            "WHERE sr.student_id = :studentId AND sr.exam_type = :examType AND sr.is_deleted = 0 " +
            "ORDER BY co.academic_year DESC, co.semester, co.course_name",
            nativeQuery = true)
    List<ScoreRecord> findByStudentIdAndExamType(
            @Param("studentId") Long studentId,
            @Param("examType") String examType);

    @Query(value = "SELECT sr.* FROM score_record sr " +
            "JOIN student s ON sr.student_id = s.id " +
            "WHERE s.class_id = :classId AND sr.exam_type = '期末' AND sr.is_deleted = 0",
            nativeQuery = true)
    List<ScoreRecord> findByClassId(@Param("classId") Long classId);

    @Query(value = "SELECT s.id, s.name, s.student_no, ci.class_name, " +
            "SUM(sr.score_value) AS total_score, " +
            "COUNT(CASE WHEN sr.score_value < 60 THEN 1 END) AS fail_count " +
            "FROM score_record sr " +
            "JOIN student s ON sr.student_id = s.id " +
            "JOIN class_info ci ON s.class_id = ci.id " +
            "WHERE s.class_id = :classId AND sr.exam_type = '期末' AND sr.is_deleted = 0 " +
            "GROUP BY s.id, s.name, s.student_no, ci.class_name " +
            "ORDER BY total_score DESC",
            nativeQuery = true)
    List<Object[]> findClassStudentStats(@Param("classId") Long classId);

    @Query(value = "SELECT " +
            "AVG(sr.score_value) AS avg_score, " +
            "COUNT(CASE WHEN sr.score_value >= 60 THEN 1 END) * 100.0 / COUNT(*) AS pass_rate, " +
            "COUNT(CASE WHEN sr.score_value < 60 THEN 1 END) AS fail_count, " +
            "COUNT(DISTINCT sr.student_id) AS total_students " +
            "FROM score_record sr " +
            "JOIN student s ON sr.student_id = s.id " +
            "WHERE s.class_id = :classId AND sr.exam_type = '期末' AND sr.is_deleted = 0",
            nativeQuery = true)
    Object[] findClassScoreOverview(@Param("classId") Long classId);

    @Query(value = "SELECT CASE " +
            "WHEN sr.score_value < 10 THEN '0-9' " +
            "WHEN sr.score_value < 20 THEN '10-19' " +
            "WHEN sr.score_value < 30 THEN '20-29' " +
            "WHEN sr.score_value < 40 THEN '30-39' " +
            "WHEN sr.score_value < 50 THEN '40-49' " +
            "WHEN sr.score_value < 60 THEN '50-59' " +
            "WHEN sr.score_value < 70 THEN '60-69' " +
            "WHEN sr.score_value < 80 THEN '70-79' " +
            "WHEN sr.score_value < 90 THEN '80-89' " +
            "ELSE '90-100' END AS range_label, " +
            "COUNT(*) AS cnt " +
            "FROM score_record sr " +
            "JOIN student s ON sr.student_id = s.id " +
            "WHERE s.class_id = :classId AND sr.exam_type = '期末' AND sr.is_deleted = 0 " +
            "GROUP BY range_label " +
            "ORDER BY range_label",
            nativeQuery = true)
    List<Object[]> findClassScoreDistribution(@Param("classId") Long classId);

    @Query(value = "SELECT co.course_name, " +
            "AVG(sr.score_value) AS avg_score, " +
            "COUNT(CASE WHEN sr.score_value >= 60 THEN 1 END) * 100.0 / COUNT(*) AS pass_rate, " +
            "COUNT(CASE WHEN sr.score_value < 60 THEN 1 END) AS fail_count " +
            "FROM score_record sr " +
            "JOIN student s ON sr.student_id = s.id " +
            "JOIN course co ON sr.course_id = co.id " +
            "WHERE s.class_id = :classId AND sr.exam_type = '期末' AND sr.is_deleted = 0 " +
            "GROUP BY co.id, co.course_name " +
            "ORDER BY co.course_name",
            nativeQuery = true)
    List<Object[]> findClassCourseRankings(@Param("classId") Long classId);

    @Query(value = "SELECT sr.* FROM score_record sr " +
            "WHERE sr.student_id = :studentId AND sr.course_id = :courseId AND sr.exam_type = :examType AND sr.is_deleted = 0",
            nativeQuery = true)
    List<ScoreRecord> findByStudentIdAndCourseIdAndExamType(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId,
            @Param("examType") String examType);

    // ========== 全年级排名查询 ==========

    @Query(value = "SELECT s.id, s.name, ci.class_name, SUM(sr.score_value) AS total_score, " +
            "ROUND(AVG(sr.score_value), 1) AS avg_score " +
            "FROM score_record sr " +
            "JOIN student s ON sr.student_id = s.id " +
            "JOIN class_info ci ON s.class_id = ci.id " +
            "JOIN course co ON sr.course_id = co.id " +
            "WHERE (:examType IS NULL OR :examType = '' OR sr.exam_type = :examType) " +
            "AND (:academicYear IS NULL OR :academicYear = '' OR co.academic_year = :academicYear) " +
            "AND (:semester IS NULL OR :semester = '' OR co.semester = :semester) " +
            "AND sr.is_deleted = 0 " +
            "GROUP BY s.id, s.name, ci.class_name " +
            "ORDER BY total_score DESC",
            nativeQuery = true)
    List<Object[]> findGradeTotalRanking(
            @Param("academicYear") String academicYear,
            @Param("semester") String semester,
            @Param("examType") String examType);

    @Query(value = "SELECT s.id, s.name, ci.class_name, sr.score_value " +
            "FROM score_record sr " +
            "JOIN student s ON sr.student_id = s.id " +
            "JOIN class_info ci ON s.class_id = ci.id " +
            "JOIN course co ON sr.course_id = co.id " +
            "WHERE co.course_name = :courseName " +
            "AND (:examType IS NULL OR :examType = '' OR sr.exam_type = :examType) " +
            "AND (:academicYear IS NULL OR :academicYear = '' OR co.academic_year = :academicYear) " +
            "AND (:semester IS NULL OR :semester = '' OR co.semester = :semester) " +
            "AND sr.is_deleted = 0 " +
            "ORDER BY sr.score_value DESC",
            nativeQuery = true)
    List<Object[]> findCourseRanking(
            @Param("courseName") String courseName,
            @Param("academicYear") String academicYear,
            @Param("semester") String semester,
            @Param("examType") String examType);

    // ========== 成绩管理查询 ==========

    @Query(value = "SELECT sr.id, sr.student_id, s.name AS student_name, s.student_no, " +
            "ci.class_name, sr.course_id, co.course_name, sr.score_value, sr.exam_type, " +
            "sr.remark, sr.create_time " +
            "FROM score_record sr " +
            "JOIN student s ON sr.student_id = s.id " +
            "JOIN class_info ci ON s.class_id = ci.id " +
            "JOIN course co ON sr.course_id = co.id " +
            "WHERE sr.is_deleted = 0 " +
            "AND (:academicYear IS NULL OR :academicYear = '' OR co.academic_year = :academicYear) " +
            "AND (:semester IS NULL OR :semester = '' OR co.semester = :semester) " +
            "AND (:courseId IS NULL OR sr.course_id = :courseId) " +
            "AND (:examType IS NULL OR :examType = '' OR sr.exam_type = :examType) " +
            "AND (:keyword IS NULL OR :keyword = '' OR s.name LIKE CONCAT('%', :keyword, '%') OR s.student_no LIKE CONCAT('%', :keyword, '%')) " +
            "ORDER BY sr.create_time DESC",
            countQuery = "SELECT COUNT(*) " +
                    "FROM score_record sr " +
                    "JOIN student s ON sr.student_id = s.id " +
                    "JOIN course co ON sr.course_id = co.id " +
                    "WHERE sr.is_deleted = 0 " +
                    "AND (:academicYear IS NULL OR :academicYear = '' OR co.academic_year = :academicYear) " +
                    "AND (:semester IS NULL OR :semester = '' OR co.semester = :semester) " +
                    "AND (:courseId IS NULL OR sr.course_id = :courseId) " +
                    "AND (:examType IS NULL OR :examType = '' OR sr.exam_type = :examType) " +
                    "AND (:keyword IS NULL OR :keyword = '' OR s.name LIKE CONCAT('%', :keyword, '%') OR s.student_no LIKE CONCAT('%', :keyword, '%'))",
            nativeQuery = true)
    org.springframework.data.domain.Page<Object[]> findScoreRecordsWithFilters(
            @Param("academicYear") String academicYear,
            @Param("semester") String semester,
            @Param("courseId") Long courseId,
            @Param("examType") String examType,
            @Param("keyword") String keyword,
            org.springframework.data.domain.Pageable pageable);

    @Query(value = "SELECT COUNT(*) FROM score_record sr " +
            "JOIN course co ON sr.course_id = co.id " +
            "WHERE sr.is_deleted = 0 " +
            "AND (:academicYear IS NULL OR :academicYear = '' OR co.academic_year = :academicYear) " +
            "AND (:semester IS NULL OR :semester = '' OR co.semester = :semester) " +
            "AND (:courseId IS NULL OR sr.course_id = :courseId) " +
            "AND (:examType IS NULL OR :examType = '' OR sr.exam_type = :examType)",
            nativeQuery = true)
    long countScoreRecordsWithFilters(
            @Param("academicYear") String academicYear,
            @Param("semester") String semester,
            @Param("courseId") Long courseId,
            @Param("examType") String examType);

    @Query(value = "SELECT AVG(sr.score_value) FROM score_record sr " +
            "JOIN course co ON sr.course_id = co.id " +
            "WHERE sr.is_deleted = 0 " +
            "AND (:academicYear IS NULL OR :academicYear = '' OR co.academic_year = :academicYear) " +
            "AND (:semester IS NULL OR :semester = '' OR co.semester = :semester) " +
            "AND (:courseId IS NULL OR sr.course_id = :courseId) " +
            "AND (:examType IS NULL OR :examType = '' OR sr.exam_type = :examType)",
            nativeQuery = true)
    BigDecimal avgScoreRecordsWithFilters(
            @Param("academicYear") String academicYear,
            @Param("semester") String semester,
            @Param("courseId") Long courseId,
            @Param("examType") String examType);

    @Query(value = "SELECT COUNT(*) FROM score_record sr " +
            "JOIN course co ON sr.course_id = co.id " +
            "WHERE sr.is_deleted = 0 AND sr.score_value >= 60 " +
            "AND (:academicYear IS NULL OR :academicYear = '' OR co.academic_year = :academicYear) " +
            "AND (:semester IS NULL OR :semester = '' OR co.semester = :semester) " +
            "AND (:courseId IS NULL OR sr.course_id = :courseId) " +
            "AND (:examType IS NULL OR :examType = '' OR sr.exam_type = :examType)",
            nativeQuery = true)
    long countPassingWithFilters(
            @Param("academicYear") String academicYear,
            @Param("semester") String semester,
            @Param("courseId") Long courseId,
            @Param("examType") String examType);

    @Query(value = "SELECT COUNT(*) FROM score_record sr " +
            "JOIN course co ON sr.course_id = co.id " +
            "WHERE sr.is_deleted = 0 AND sr.score_value < 60 " +
            "AND (:academicYear IS NULL OR :academicYear = '' OR co.academic_year = :academicYear) " +
            "AND (:semester IS NULL OR :semester = '' OR co.semester = :semester) " +
            "AND (:courseId IS NULL OR sr.course_id = :courseId) " +
            "AND (:examType IS NULL OR :examType = '' OR sr.exam_type = :examType)",
            nativeQuery = true)
    long countFailingWithFilters(
            @Param("academicYear") String academicYear,
            @Param("semester") String semester,
            @Param("courseId") Long courseId,
            @Param("examType") String examType);

    @Query(value = "SELECT sr.*, co.course_name, co.academic_year, co.semester " +
            "FROM score_record sr " +
            "JOIN course co ON sr.course_id = co.id " +
            "WHERE sr.student_id = :studentId AND sr.is_deleted = 0 " +
            "AND sr.exam_type IN ('期中', '期末') " +
            "ORDER BY co.academic_year, co.semester, sr.exam_type, co.course_name",
            nativeQuery = true)
    List<ScoreRecord> findByStudentIdOrderedByTime(@Param("studentId") Long studentId);

    @Query(value = "SELECT sr.* FROM score_record sr " +
            "JOIN course co ON sr.course_id = co.id " +
            "WHERE sr.student_id = :studentId AND sr.is_deleted = 0 " +
            "AND sr.exam_type IN ('期中', '期末') " +
            "AND (:academicYear IS NULL OR :academicYear = '' OR co.academic_year = :academicYear) " +
            "AND (:semester IS NULL OR :semester = '' OR co.semester = :semester) " +
            "AND (:examType IS NULL OR :examType = '' OR sr.exam_type = :examType) " +
            "ORDER BY sr.create_time DESC",
            nativeQuery = true)
    List<ScoreRecord> findByStudentIdWithFilters(
            @Param("studentId") Long studentId,
            @Param("academicYear") String academicYear,
            @Param("semester") String semester,
            @Param("examType") String examType);

    @Query(value = "SELECT sr.*, co.course_name, co.academic_year, co.semester " +
            "FROM score_record sr " +
            "JOIN course co ON sr.course_id = co.id " +
            "WHERE sr.student_id = :studentId AND sr.is_deleted = 0 " +
            "AND sr.exam_type IN ('期中', '期末') " +
            "AND (:academicYear IS NULL OR :academicYear = '' OR co.academic_year = :academicYear) " +
            "AND (:semester IS NULL OR :semester = '' OR co.semester = :semester) " +
            "AND (:examType IS NULL OR :examType = '' OR sr.exam_type = :examType) " +
            "ORDER BY co.academic_year, co.semester, sr.exam_type, co.course_name",
            nativeQuery = true)
    List<ScoreRecord> findByStudentIdOrderedWithFilters(
            @Param("studentId") Long studentId,
            @Param("academicYear") String academicYear,
            @Param("semester") String semester,
            @Param("examType") String examType);
}
