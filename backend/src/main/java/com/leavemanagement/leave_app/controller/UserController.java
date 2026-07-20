package com.leavemanagement.leave_app.controller;

import com.leavemanagement.leave_app.model.User;
import com.leavemanagement.leave_app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/profile-pictures/";

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getUserProfile(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .map(user -> {
                    Map<String, Object> profile = new HashMap<>();
                    profile.put("fullName", user.getFullName());
                    profile.put("email", user.getEmail());
                    profile.put("username", user.getUsername());
                    profile.put("department", user.getDepartment());
                    profile.put("role", user.getRole().toString());
                    profile.put("employeeCode", generateEmployeeCode(user));
                    profile.put("profilePicture", user.getProfilePicture());
                    return ResponseEntity.ok(profile);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/profile-picture")
    public ResponseEntity<Map<String, String>> uploadProfilePicture(
            @RequestParam("profilePicture") MultipartFile file,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        String username = authentication.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(errorResponse("Please select a file to upload"));
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body(errorResponse("Please upload a valid image file"));
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest().body(errorResponse("File size must be less than 5MB"));
        }

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = username + "_" + UUID.randomUUID() + fileExtension;

            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath);

            String profilePicturePath = "/uploads/profile-pictures/" + newFilename;
            user.setProfilePicture(profilePicturePath);
            userRepository.save(user);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Profile picture updated successfully");
            response.put("profilePicture", profilePicturePath);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(errorResponse("Failed to upload file"));
        }
    }

    private String generateEmployeeCode(User user) {
        String deptCode = user.getDepartment().substring(0, Math.min(3, user.getDepartment().length())).toUpperCase();
        String userIdStr = String.format("%03d", Math.abs(user.getUsername().hashCode() % 1000));
        return deptCode + userIdStr;
    }

    private Map<String, String> errorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}