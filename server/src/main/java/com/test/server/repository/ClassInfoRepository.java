package com.test.server.repository;

import com.test.server.entity.ClassInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassInfoRepository extends JpaRepository<ClassInfo, Long> {

    List<ClassInfo> findByGrade(Integer grade);

    List<ClassInfo> findByMajor(String major);

    List<ClassInfo> findByGradeAndMajor(Integer grade, String major);

    @Query("SELECT c FROM ClassInfo c WHERE c.isDeleted = 0 AND c.className LIKE %:keyword%")
    List<ClassInfo> findByKeyword(@Param("keyword") String keyword);

    @Query("SELECT COUNT(s) FROM Student s WHERE s.classId = :classId AND s.isDeleted = 0")
    long countByClassId(@Param("classId") Long classId);
}
