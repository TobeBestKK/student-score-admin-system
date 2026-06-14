package com.test.server.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.test.server.dto.StudentCreateDTO;
import com.test.server.dto.StudentDTO;
import com.test.server.dto.StudentUpdateDTO;
import com.test.server.entity.ClassInfo;
import com.test.server.entity.Student;
import com.test.server.repository.ClassInfoRepository;
import com.test.server.repository.StudentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final ClassInfoRepository classInfoRepository;

    public List<StudentDTO> getAllStudents() {
        return studentRepository.findAll(Sort.by("studentNo").ascending()).stream()
                .filter(s -> s.getIsDeleted() == null || s.getIsDeleted() == 0)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Page<StudentDTO> getStudentsPage(Integer grade, Long classId, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("studentNo").ascending());
        Page<Student> studentPage;

        if (keyword != null && !keyword.isEmpty()) {
            studentPage = studentRepository.findByKeyword(keyword, pageable);
        } else if (classId != null) {
            studentPage = studentRepository.findByClassIdAndIsDeleted(classId, 0, pageable);
        } else if (grade != null) {
            studentPage = studentRepository.findByGradeAndIsDeleted(grade, 0, pageable);
        } else {
            studentPage = studentRepository.findByIsDeleted(0, pageable);
        }

        return studentPage.map(this::convertToDTO);
    }

    public StudentDTO getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("学生不存在"));
        if (student.getIsDeleted() != null && student.getIsDeleted() == 1) {
            throw new RuntimeException("学生不存在");
        }
        return convertToDTO(student);
    }

    @Transactional
    public StudentDTO createStudent(StudentCreateDTO dto) {
        if (studentRepository.findByStudentNo(dto.getStudentNo()).isPresent()) {
            throw new RuntimeException("学号已存在");
        }

        ClassInfo classInfo = classInfoRepository.findById(dto.getClassId())
                .orElseThrow(() -> new RuntimeException("班级不存在"));

        Student student = new Student();
        student.setStudentNo(dto.getStudentNo());
        student.setName(dto.getName());
        student.setGender(dto.getGender() != null ? dto.getGender() : 1);
        student.setClassId(dto.getClassId());
        student.setPhone(dto.getPhone());
        student.setEmail(dto.getEmail());
        student.setEnrollmentYear(dto.getEnrollmentYear());
        student.setCreateTime(LocalDateTime.now());
        student.setUpdateTime(LocalDateTime.now());
        student.setIsDeleted(0);

        Student saved = studentRepository.save(student);
        return convertToDTO(saved);
    }

    @Transactional
    public StudentDTO updateStudent(Long id, StudentUpdateDTO dto) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("学生不存在"));

        if (student.getIsDeleted() != null && student.getIsDeleted() == 1) {
            throw new RuntimeException("学生不存在");
        }

        if (dto.getStudentNo() != null && !dto.getStudentNo().equals(student.getStudentNo())) {
            if (studentRepository.findByStudentNo(dto.getStudentNo()).isPresent()) {
                throw new RuntimeException("学号已存在");
            }
            student.setStudentNo(dto.getStudentNo());
        }

        if (dto.getName() != null) {
            student.setName(dto.getName());
        }
        if (dto.getGender() != null) {
            student.setGender(dto.getGender());
        }
        if (dto.getClassId() != null) {
            classInfoRepository.findById(dto.getClassId())
                    .orElseThrow(() -> new RuntimeException("班级不存在"));
            student.setClassId(dto.getClassId());
        }
        if (dto.getPhone() != null) {
            student.setPhone(dto.getPhone());
        }
        if (dto.getEmail() != null) {
            student.setEmail(dto.getEmail());
        }
        if (dto.getEnrollmentYear() != null) {
            student.setEnrollmentYear(dto.getEnrollmentYear());
        }

        student.setUpdateTime(LocalDateTime.now());
        Student saved = studentRepository.save(student);
        return convertToDTO(saved);
    }

    @Transactional
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("学生不存在"));

        if (student.getIsDeleted() != null && student.getIsDeleted() == 1) {
            throw new RuntimeException("学生不存在");
        }

        student.setIsDeleted(1);
        student.setUpdateTime(LocalDateTime.now());
        studentRepository.save(student);
    }

    private StudentDTO convertToDTO(Student student) {
        String className = null;
        if (student.getClassId() != null) {
            className = classInfoRepository.findById(student.getClassId())
                    .map(ClassInfo::getClassName)
                    .orElse(null);
        }

        return new StudentDTO(
                student.getId(),
                student.getStudentNo(),
                student.getName(),
                student.getGender(),
                student.getClassId(),
                className,
                student.getPhone(),
                student.getEmail(),
                student.getEnrollmentYear()
        );
    }
}