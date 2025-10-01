package com.leavemanagement.leave_app.controller;

import com.leavemanagement.leave_app.model.User;
import com.leavemanagement.leave_app.model.LeaveRequest;
import com.leavemanagement.leave_app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/test")
public class EmailTestController {

    @Autowired
    private UserRepository userRepository;

    /**
     * Test endpoint to verify email functionality (simulation only, no actual email)
     */
    @PostMapping("/email")
    public ResponseEntity<Map<String, Object>> testEmail(@RequestBody Map<String, String> request) {
        try {
            String userEmail = request.get("email");
            String testType = request.getOrDefault("type", "approval");

            if (userEmail == null || userEmail.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Email address is required"
                ));
            }

            // Find user by email
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            if (!userOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found with email: " + userEmail,
                    "availableUsers", getAllUserEmails()
                ));
            }

            User user = userOpt.get();

            // Create a sample leave request for testing (simulation only)
            LeaveRequest testLeaveRequest = new LeaveRequest();
            testLeaveRequest.setEmployeeName(user.getFullName());
            testLeaveRequest.setEmployeeId(user.getId());
            testLeaveRequest.setStartDate(LocalDate.now().plusDays(7));
            testLeaveRequest.setEndDate(LocalDate.now().plusDays(9));
            testLeaveRequest.setReason("Test email notification - Annual Leave");
            testLeaveRequest.setLeaveType("Annual");
            testLeaveRequest.setStatus(testType.equals("approval") ? "Approved" : "Rejected");

            // ⚠️ Email sending removed — just simulate response
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Simulated test " + testType + " email (no actual email sent)",
                "user", Map.of(
                    "name", user.getFullName(),
                    "email", user.getEmail(),
                    "department", user.getDepartment() != null ? user.getDepartment() : "N/A"
                ),
                "leaveRequest", Map.of(
                    "leaveType", testLeaveRequest.getLeaveType(),
                    "status", testLeaveRequest.getStatus(),
                    "startDate", testLeaveRequest.getStartDate(),
                    "endDate", testLeaveRequest.getEndDate(),
                    "reason", testLeaveRequest.getReason()
                )
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Error in test simulation: " + e.getMessage(),
                "error", e.getClass().getSimpleName()
            ));
        }
    }

    /**
     * Get list of all user emails for testing
     */
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        try {
            List<User> allUsers = userRepository.findAll();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "userCount", allUsers.size(),
                "users", allUsers.stream().map(user -> Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "fullName", user.getFullName(),
                    "email", user.getEmail(),
                    "department", user.getDepartment() != null ? user.getDepartment() : "N/A",
                    "role", user.getRole() != null ? user.getRole().toString() : "N/A"
                )).toList()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Error fetching users: " + e.getMessage()
            ));
        }
    }

    private List<String> getAllUserEmails() {
        return userRepository.findAll().stream()
                .map(User::getEmail)
                .filter(email -> email != null && !email.isEmpty())
                .toList();
    }
}
