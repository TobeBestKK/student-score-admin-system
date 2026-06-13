package com.test.server.service;

import com.test.server.dto.LoginRequest;
import com.test.server.dto.LoginResponse;
import com.test.server.entity.Student;
import com.test.server.entity.Teacher;
import com.test.server.entity.User;
import com.test.server.repository.StudentRepository;
import com.test.server.repository.TeacherRepository;
import com.test.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername()).orElse(null);

        if (user == null) {
            return new LoginResponse(false, "账号不存在", null, null);
        }

        if (user.getStatus() == 0) {
            return new LoginResponse(false, "账号已被禁用", null, null);
        }

        if (!request.getPassword().equals(user.getPassword())) {
            return new LoginResponse(false, "密码错误", null, null);
        }

        String roleCode = user.getRole() != null ? user.getRole().getRoleCode() : null;
        if (roleCode == null || !roleCode.equalsIgnoreCase(request.getRole())) {
            return new LoginResponse(false, "账号角色不匹配，请选择正确的登录方式", null, null);
        }

        String name = null;
        String className = null;
        String studentNo = null;
        String teacherNo = null;

        if ("student".equalsIgnoreCase(request.getRole())) {
            Student student = studentRepository.findByUserId(user.getId()).orElse(null);
            if (student != null) {
                name = student.getName();
                studentNo = student.getStudentNo();
                if (student.getClassInfo() != null) {
                    className = student.getClassInfo().getClassName();
                }
            }
        } else if ("teacher".equalsIgnoreCase(request.getRole())) {
            Teacher teacher = teacherRepository.findByUserId(user.getId()).orElse(null);
            if (teacher != null) {
                name = teacher.getName();
                teacherNo = teacher.getTeacherNo();
            }
        }

        String token = generateToken(user);

        Long id = user.getId();
        if ("student".equalsIgnoreCase(request.getRole())) {
            Student student = studentRepository.findByUserId(user.getId()).orElse(null);
            if (student != null) {
                id = student.getId();
            }
        }

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                id,
                user.getUsername(),
                name,
                roleCode,
                roleCode,
                className,
                studentNo,
                teacherNo
        );

        return new LoginResponse(true, "登录成功", token, userInfo);
    }

    private String generateToken(User user) {
        return "token_" + user.getId() + "_" + System.currentTimeMillis();
    }
}