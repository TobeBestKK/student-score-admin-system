package com.test.server.repository;

import com.test.server.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    Page<Student> findByIsDeleted(Integer isDeleted, Pageable pageable);

    Page<Student> findByClassIdAndIsDeleted(Long classId, Integer isDeleted, Pageable pageable);

    @Query("SELECT s FROM Student s JOIN s.classInfo ci WHERE ci.grade = :grade AND s.isDeleted = :isDeleted")
    Page<Student> findByGradeAndIsDeleted(@Param("grade") Integer grade, @Param("isDeleted") Integer isDeleted, Pageable pageable);

    @Query("SELECT s FROM Student s WHERE s.isDeleted = 0 AND (s.name LIKE %:keyword% OR s.studentNo LIKE %:keyword%)")
    Page<Student> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
}