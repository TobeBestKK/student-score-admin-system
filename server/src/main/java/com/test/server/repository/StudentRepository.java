package com.test.server.repository;

import com.test.server.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUserId(Long userId);
    Optional<Student> findByStudentNo(String studentNo);

    List<Student> findByClassId(Long classId);

    @Query("SELECT COUNT(s) FROM Student s WHERE s.classId = :classId AND s.isDeleted = 0")
    long countByClassId(@Param("classId") Long classId);

    @Query("SELECT s FROM Student s JOIN s.classInfo ci WHERE ci.grade = :grade AND s.isDeleted = 0")
    List<Student> findByGrade(@Param("grade") Long grade);
}