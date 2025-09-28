package com.leavemanagment.leave_app.controller;

import com.leavemanagment.leave_app.model.User;
import com.leavemanagment.leave_app.repository.UserRepository;
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

    private final String UPLOAD_DIR = "src/main/resources/static/uploads/profile-pictures/";

    @GetMapping("/test-auth")
    public ResponseEntity<String> testAuth(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.ok("❌ No authentication");
        }
        return ResponseEntity.ok("✅ Authenticated as: " + authentication.getName());
    }

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getUserProfile(Authentication authentication) {
        try {
            if (authentication == null) {
                System.err.println("❌ Authentication is null");
                return ResponseEntity.status(401).build();
            }
            
            String username = authentication.getName();
            System.out.println("🔍 Getting profile for user: " + username);
            
            Optional<User> userOpt = userRepository.findByUsername(username);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                System.out.println("✅ Found user: " + user.getFullName() + " (" + user.getEmail() + ")");
                
                Map<String, Object> profile = new HashMap<>();
                
                profile.put("fullName", user.getFullName());
                profile.put("email", user.getEmail());
                profile.put("username", user.getUsername());
                profile.put("department", user.getDepartment());
                profile.put("role", user.getRole().toString());
                profile.put("employeeCode", generateEmployeeCode(user));
                profile.put("profilePicture", user.getProfilePicture());
                
                System.out.println("📤 Sending profile: " + profile);
                return ResponseEntity.ok(profile);
            } else {
                System.err.println("❌ User not found: " + username);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Error fetching user profile: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/profile-picture")
    public ResponseEntity<Map<String, String>> uploadProfilePicture(
            @RequestParam("profilePicture") MultipartFile file,
            Authentication authentication) {
        
        try {
            String username = authentication.getName();
            Optional<User> userOpt = userRepository.findByUsername(username);
            
            if (!userOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            User user = userOpt.get();
            
            // Validate file
            if (file.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Please select a file to upload");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Check file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Please upload a valid image file");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Check file size (5MB max)
            if (file.getSize() > 5 * 1024 * 1024) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "File size must be less than 5MB");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = username + "_" + UUID.randomUUID().toString() + fileExtension;
            
            // Save file
            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath);
            
            // Update user profile picture path
            String profilePicturePath = "/uploads/profile-pictures/" + newFilename;
            user.setProfilePicture(profilePicturePath);
            userRepository.save(user);
            
            System.out.println("💾 Saved profile picture path to database: " + profilePicturePath);
            System.out.println("📁 File saved to: " + filePath.toString());
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Profile picture updated successfully");
            response.put("profilePicture", profilePicturePath);
            
            System.out.println("📤 Sending response: " + response);
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            System.err.println("Error uploading profile picture: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to upload file");
            return ResponseEntity.internalServerError().body(error);
        } catch (Exception e) {
            System.err.println("Error uploading profile picture: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "An unexpected error occurred");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<Map<String, String>> updateProfile(
            @RequestBody Map<String, String> updates,
            Authentication authentication) {
        
        try {
            String username = authentication.getName();
            Optional<User> userOpt = userRepository.findByUsername(username);
            
            if (!userOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            User user = userOpt.get();
            
            // Update allowed fields
            if (updates.containsKey("fullName")) {
                user.setFullName(updates.get("fullName"));
            }
            if (updates.containsKey("email")) {
                user.setEmail(updates.get("email"));
            }
            if (updates.containsKey("department")) {
                user.setDepartment(updates.get("department"));
            }
            
            userRepository.save(user);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Profile updated successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error updating profile: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update profile");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    private String generateEmployeeCode(User user) {
        // Generate employee code based on department and user ID
        String deptCode = user.getDepartment().substring(0, Math.min(3, user.getDepartment().length())).toUpperCase();
        String userIdStr = String.format("%03d", Math.abs(user.getUsername().hashCode() % 1000));
        return deptCode + userIdStr;
    }
}