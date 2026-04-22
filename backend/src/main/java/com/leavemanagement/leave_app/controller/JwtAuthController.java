package com.leavemanagement.leave_app.controller;

import com.leavemanagement.leave_app.model.Role;
import com.leavemanagement.leave_app.model.User;
import com.leavemanagement.leave_app.repository.UserRepository;
import com.leavemanagement.leave_app.security.JwtUtils;
import com.leavemanagement.leave_app.service.EmployeeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class JwtAuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmployeeService employeeService;

    public static class AuthRequest {
        public String username;
        public String password;
    }

    public static class SignupRequest {
        public String fullName;
        public String username;
        public String email;
        public String password;
        public String department;
        public String role;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username, request.password));

            User user = userRepository.findByUsername(request.username).orElseThrow();
            String token = jwtUtils.generateToken(user.getUsername(), List.of(user.getRole().name()));

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("fullName", user.getFullName());
            response.put("role", user.getRole().name());

            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid username or password");
            return ResponseEntity.status(401).body(error);
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@RequestBody SignupRequest request) {
        Map<String, Object> ret = new HashMap<>();
        if (userRepository.findByUsername(request.username).isPresent()) {
            ret.put("error", "Username already exists");
            return ResponseEntity.badRequest().body(ret);
        }
        if (userRepository.findByEmail(request.email).isPresent()) {
            ret.put("error", "Email already exists");
            return ResponseEntity.badRequest().body(ret);
        }

        User newUser = new User();
        newUser.setUsername(request.username);
        newUser.setEmail(request.email);
        newUser.setPassword(passwordEncoder.encode(request.password));
        newUser.setFullName(request.fullName);
        newUser.setDepartment(request.department);
        newUser.setRole("HR".equalsIgnoreCase(request.role) ? Role.HR : Role.EMPLOYEE);

        userRepository.save(newUser);
        employeeService.createEmployeeFromUser(newUser);

        ret.put("success", true);
        ret.put("message", "User registered. Please login.");
        return ResponseEntity.ok(ret);
    }
}
