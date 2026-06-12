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

    @Query("SELECT MAX(sr.scoreValue) FROM ScoreRecord sr WHERE sr.courseId IN :courseIds AND sr.examType = '期末'")
    BigDecimal findMaxScoreByCourseIds(@Param("courseIds") List<Long> courseIds);

    @Query("SELECT COUNT(DISTINCT sr.studentId) FROM ScoreRecord sr WHERE sr.courseId IN :courseIds AND sr.examType = '期末' AND sr.scoreValue >= 60")
    long countPassingStudentsByCourseIds(@Param("courseIds") List<Long> courseIds);

    @Query(value = "SELECT CASE " +
            "WHEN score_value < 60 THEN '0-59' " +
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
            "WHERE course_id IN :courseIds AND exam_type = '期末' AND score_value >= 60 AND score_value < 70 AND is_deleted = 0",
            nativeQuery = true)
    List<Object[]> findWarningsByCourseIds(@Param("courseIds") List<Long> courseIds);

    @Query(value = "SELECT sr.id, s.name AS student_name, co.course_name, sr.score_value, sr.exam_type, sr.create_time " +
            "FROM score_record sr " +
            "JOIN student s ON sr.student_id = s.id " +
            "JOIN course co ON sr.course_id = co.id " +
            "WHERE sr.course_id IN :courseIds AND sr.is_deleted = 0 " +
            "ORDER BY sr.create_time DESC " +
            "LIMIT :limit",
            nativeQuery = true)
    List<Object[]> findRecentRecordsByCourseIds(@Param("courseIds") List<Long> courseIds, @Param("limit") int limit);

    @Query(value = "SELECT DISTINCT co.academic_year, co.semester " +
            "FROM course co " +
            "WHERE co.is_deleted = 0 AND co.academic_year IS NOT NULL " +
            "ORDER BY co.academic_year DESC, co.semester",
            nativeQuery = true)
    List<Object[]> findDistinctYearSemesters();
}
