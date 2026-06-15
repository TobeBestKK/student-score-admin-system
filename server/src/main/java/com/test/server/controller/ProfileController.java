package com.test.server.controller;

import com.test.server.dto.ProfileDTO;
import com.test.server.entity.Student;
import com.test.server.entity.Teacher;
import com.test.server.entity.User;
import com.test.server.repository.StudentRepository;
import com.test.server.repository.TeacherRepository;
import com.test.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    @GetMapping
    public ResponseEntity<?> getProfile(@RequestParam(required = false) Long userId,
                                        @RequestParam(required = false) Long studentId,
                                        @RequestParam(required = false) Long teacherId,
                                        @RequestParam(required = false) String username) {
        try {
            ProfileDTO profileDTO = new ProfileDTO();
            
            // 优先使用username查询
            if (username != null && !username.isEmpty()) {
                User user = userRepository.findByUsername(username).orElse(null);
                if (user == null) {
                    return buildErrorResponse("用户不存在");
                }
                return buildProfileResponse(user, profileDTO);
            }
            
            // 使用studentId查询
            if (studentId != null) {
                Student student = studentRepository.findById(studentId).orElse(null);
                if (student == null) {
                    return buildErrorResponse("学生不存在");
                }
                profileDTO.setId(student.getId());
                profileDTO.setStudentId(student.getStudentNo());
                profileDTO.setName(student.getName());
                profileDTO.setEmail(student.getEmail());
                profileDTO.setPhone(student.getPhone());
                profileDTO.setUsername(student.getUser() != null ? student.getUser().getUsername() : null);
                return ResponseEntity.ok(profileDTO);
            }
            
            // 使用teacherId查询
            if (teacherId != null) {
                Teacher teacher = teacherRepository.findById(teacherId).orElse(null);
                if (teacher == null) {
                    return buildErrorResponse("教师不存在");
                }
                profileDTO.setId(teacher.getId());
                profileDTO.setPosition(teacher.getTitle());
                profileDTO.setName(teacher.getName());
                profileDTO.setEmail(teacher.getEmail());
                profileDTO.setPhone(teacher.getPhone());
                profileDTO.setUsername(teacher.getUser() != null ? teacher.getUser().getUsername() : null);
                return ResponseEntity.ok(profileDTO);
            }
            
            // 使用userId查询
            if (userId != null) {
                User user = userRepository.findById(userId).orElse(null);
                if (user == null) {
                    return buildErrorResponse("用户不存在");
                }
                return buildProfileResponse(user, profileDTO);
            }
            
            return buildErrorResponse("缺少查询参数");
        } catch (Exception e) {
            e.printStackTrace();
            return buildErrorResponse("获取用户信息失败: " + e.getMessage());
        }
    }

    private ResponseEntity<?> buildProfileResponse(User user, ProfileDTO profileDTO) {
        profileDTO.setId(user.getId());
        profileDTO.setUsername(user.getUsername());

        if (user.getRole() != null) {
            String roleCode = user.getRole().getRoleCode();
            if ("student".equalsIgnoreCase(roleCode)) {
                Student student = studentRepository.findByUserId(user.getId()).orElse(null);
                if (student != null) {
                    profileDTO.setName(student.getName());
                    profileDTO.setStudentId(student.getStudentNo());
                    profileDTO.setEmail(student.getEmail());
                    profileDTO.setPhone(student.getPhone());
                }
            } else if ("teacher".equalsIgnoreCase(roleCode)) {
                Teacher teacher = teacherRepository.findByUserId(user.getId()).orElse(null);
                if (teacher != null) {
                    profileDTO.setName(teacher.getName());
                    profileDTO.setPosition(teacher.getTitle());
                    profileDTO.setEmail(teacher.getEmail());
                    profileDTO.setPhone(teacher.getPhone());
                }
            }
        }

        return ResponseEntity.ok(profileDTO);
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", message);
        return ResponseEntity.ok(error);
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(@RequestBody ProfileDTO profileDTO,
                                           @RequestParam(required = false) Long studentId,
                                           @RequestParam(required = false) Long teacherId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 使用studentId更新
            if (studentId != null) {
                Student student = studentRepository.findById(studentId).orElse(null);
                if (student == null) {
                    return buildErrorResponse("学生不存在");
                }
                if (profileDTO.getName() != null) {
                    student.setName(profileDTO.getName());
                }
                if (profileDTO.getPhone() != null) {
                    student.setPhone(profileDTO.getPhone());
                }
                studentRepository.save(student);
                response.put("success", true);
                response.put("message", "更新成功");
                return ResponseEntity.ok(response);
            }
            
            // 使用teacherId更新
            if (teacherId != null) {
                Teacher teacher = teacherRepository.findById(teacherId).orElse(null);
                if (teacher == null) {
                    return buildErrorResponse("教师不存在");
                }
                if (profileDTO.getName() != null) {
                    teacher.setName(profileDTO.getName());
                }
                if (profileDTO.getPhone() != null) {
                    teacher.setPhone(profileDTO.getPhone());
                }
                teacherRepository.save(teacher);
                response.put("success", true);
                response.put("message", "更新成功");
                return ResponseEntity.ok(response);
            }
            
            return buildErrorResponse("缺少用户ID参数");
        } catch (Exception e) {
            e.printStackTrace();
            return buildErrorResponse("更新失败: " + e.getMessage());
        }
    }
}
