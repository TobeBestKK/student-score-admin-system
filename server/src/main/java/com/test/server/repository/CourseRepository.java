package com.test.server.repository;

import com.test.server.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByTeacherId(Long teacherId);

    @Query("SELECT DISTINCT c.academicYear FROM Course c WHERE c.academicYear IS NOT NULL ORDER BY c.academicYear DESC")
    List<String> findDistinctAcademicYears();

    @Query("SELECT DISTINCT c.semester FROM Course c WHERE c.semester IS NOT NULL ORDER BY c.semester")
    List<String> findDistinctSemesters();

    @Query("SELECT DISTINCT c.academicYear, c.semester FROM Course c WHERE c.academicYear IS NOT NULL ORDER BY c.academicYear DESC, c.semester")
    List<Object[]> findDistinctYearSemesters();
}
