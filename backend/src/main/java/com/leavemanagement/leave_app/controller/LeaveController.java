package com.leavemanagement.leave_app.controller;

import com.leavemanagement.leave_app.model.LeaveRequest;
import com.leavemanagement.leave_app.model.User;
import com.leavemanagement.leave_app.repository.LeaveRequestRepository;
import com.leavemanagement.leave_app.repository.UserRepository;

import java.util.stream.Collectors;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.leavemanagement.leave_app.service.SmartEmailTemplateService;
import com.leavemanagement.leave_app.service.EmailService;


@RestController
@RequestMapping("/leaves")
public class LeaveController {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SmartEmailTemplateService smartEmailTemplateService;
    
    @Autowired
    private EmailService emailService;
    


    // CREATE: Add a new leave request with validation
    @PostMapping
    public ResponseEntity<LeaveRequest> createLeave(@Valid @RequestBody LeaveRequest leaveRequest) {
        System.out.println("📝 Creating new leave request for: " + leaveRequest.getEmployeeName());
        System.out.println("📅 Start Date: " + leaveRequest.getStartDate());
        System.out.println("📅 End Date: " + leaveRequest.getEndDate());
        System.out.println("📋 Reason: " + leaveRequest.getReason());
        System.out.println("🏷️ Leave Type: " + leaveRequest.getLeaveType());
        
        LeaveRequest savedLeave = leaveRequestRepository.save(leaveRequest);
        System.out.println("✅ Leave request saved with ID: " + savedLeave.getId());
        System.out.println("📊 Total leave requests in database: " + leaveRequestRepository.count());
        
        URI location = URI.create(String.format("/leaves/%s", savedLeave.getId()));
        return ResponseEntity.created(location).body(savedLeave);
    }

    // READ ALL: Get all leave requests (for admin/HR)
    @GetMapping
    public List<LeaveRequest> getAllLeaves() {
        System.out.println("Fetching all leave requests...");
        return leaveRequestRepository.findAll();
    }
    
    // READ USER LEAVES: Get leave requests for current user only
    @GetMapping("/my-leaves")
    public ResponseEntity<List<LeaveRequest>> getMyLeaves(Authentication authentication) {
        try {
            if (authentication == null) {
                System.err.println("❌ No authentication for my-leaves request");
                return ResponseEntity.status(401).build();
            }
            
            String username = authentication.getName();
            System.out.println("🔍 Fetching leaves for user: " + username);
            
            // Get user's full name from UserRepository
            Optional<com.leavemanagement.leave_app.model.User> userOpt = userRepository.findByUsername(username);
            if (!userOpt.isPresent()) {
                System.err.println("❌ User not found: " + username);
                return ResponseEntity.notFound().build();
            }
            
            String fullName = userOpt.get().getFullName();
            System.out.println("👤 Looking for leaves by employee name: " + fullName);
            
            // Find leaves by employee name
            List<LeaveRequest> userLeaves = leaveRequestRepository.findByEmployeeName(fullName);
            System.out.println("📋 Found " + userLeaves.size() + " leaves for " + fullName);
            
            return ResponseEntity.ok(userLeaves);
            
        } catch (Exception e) {
            System.err.println("Error fetching user leaves: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // READ ONE: Get a leave request by ID
    @GetMapping("/{id}")
    public ResponseEntity<LeaveRequest> getLeaveById(@PathVariable String id) {
        Optional<LeaveRequest> leave = leaveRequestRepository.findById(id);
        return leave.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // UPDATE: Update an existing leave request by ID
    @PutMapping("/{id}")
    public ResponseEntity<LeaveRequest> updateLeave(@PathVariable String id, @Valid @RequestBody LeaveRequest updatedLeave) {
        return leaveRequestRepository.findById(id)
                .map(leave -> {
                    leave.setEmployeeName(updatedLeave.getEmployeeName());
                    leave.setStartDate(updatedLeave.getStartDate());
                    leave.setEndDate(updatedLeave.getEndDate());
                    leave.setReason(updatedLeave.getReason());
                    leave.setStatus(updatedLeave.getStatus());
                    LeaveRequest savedLeave = leaveRequestRepository.save(leave);
                    return ResponseEntity.ok(savedLeave);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    @PutMapping("/{id}/approve")
    public ResponseEntity<LeaveRequest> approveLeave(@PathVariable String id, Authentication authentication) {
        Optional<LeaveRequest> optional = leaveRequestRepository.findById(id);
        if (optional.isPresent()) {
            LeaveRequest leave = optional.get();
            leave.setStatus("Approved");
            leave.setRejectionReason(null); // Clear any previous rejection reason
            LeaveRequest savedLeave = leaveRequestRepository.save(leave);
            

            
            return ResponseEntity.ok(savedLeave);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<LeaveRequest> rejectLeave(@PathVariable String id, @RequestBody(required = false) Map<String, String> requestBody, Authentication authentication) {
        try {
            System.out.println("❌ Rejecting leave request with ID: " + id);
            System.out.println("📋 Request body: " + requestBody);
            
            Optional<LeaveRequest> optional = leaveRequestRepository.findById(id);
            if (optional.isPresent()) {
                LeaveRequest leave = optional.get();
                System.out.println("📝 Found leave request for: " + leave.getEmployeeName());
                
                leave.setStatus("Rejected");
                
                // Get rejection reason from request body
                String rejectionReason = null;
                if (requestBody != null && requestBody.containsKey("rejectionReason")) {
                    rejectionReason = requestBody.get("rejectionReason");
                    System.out.println("📋 Rejection reason: " + rejectionReason);
                }
                leave.setRejectionReason(rejectionReason);
                
                LeaveRequest savedLeave = leaveRequestRepository.save(leave);
                System.out.println("✅ Leave request rejected successfully");
                return ResponseEntity.ok(savedLeave);
            } else {
                System.err.println("❌ Leave request not found with ID: " + id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("❌ Error rejecting leave request: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // New endpoint for HR to approve/reject with detailed information
    @PutMapping("/{id}/hr-action")
    public ResponseEntity<LeaveRequest> hrActionOnLeave(
            @PathVariable String id, 
            @RequestBody Map<String, String> actionRequest) {
        
        Optional<LeaveRequest> optional = leaveRequestRepository.findById(id);
        if (optional.isPresent()) {
            LeaveRequest leave = optional.get();
            String action = actionRequest.get("action");
            String reason = actionRequest.get("reason");
            
            if ("approve".equalsIgnoreCase(action)) {
                leave.setStatus("Approved");
                leave.setRejectionReason(null);
                System.out.println("✅ Leave request approved successfully");
                
            } else if ("reject".equalsIgnoreCase(action)) {
                leave.setStatus("Rejected");
                leave.setRejectionReason(reason);
                System.out.println("✅ Leave request rejected successfully");
            } else {
                return ResponseEntity.badRequest().build();
            }
            
            return ResponseEntity.ok(leaveRequestRepository.save(leave));
        }
        return ResponseEntity.notFound().build();
    }

    // duplicate logic removed

    // DYNAMIC EMAIL APPROVAL: Approve leave with email sent from HR user to Employee
    @PutMapping("/{id}/hr-approve")
    public ResponseEntity<Map<String, Object>> hrApproveLeave(@PathVariable String id, Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<LeaveRequest> optional = leaveRequestRepository.findById(id);
            if (!optional.isPresent()) {
                response.put("success", false);
                response.put("message", "Leave request not found");
                return ResponseEntity.notFound().build();
            }

            LeaveRequest leave = optional.get();
            leave.setStatus("Approved");
            leave.setRejectionReason(null);
            LeaveRequest savedLeave = leaveRequestRepository.save(leave);

            // Get employee and HR user details
            User employee = findEmployeeForLeaveRequest(leave);
            User hrUser = getCurrentHRUser(authentication);
            
            if (employee == null) {
                response.put("success", false);
                response.put("message", "Employee not found");
                return ResponseEntity.badRequest().body(response);
            }

            if (hrUser == null) {
                response.put("success", false);
                response.put("message", "HR user not found. Please ensure you're logged in as HR.");
                return ResponseEntity.badRequest().body(response);
            }

            System.out.println("📧 Sending approval email (system)");

            String emailContent = smartEmailTemplateService.generateSmartApprovalEmail(savedLeave, employee, hrUser);
            emailService.sendHtmlEmail(employee.getEmail(),
                "✅ Leave Request Approved - " + savedLeave.getLeaveType(),
                emailContent);

            response.put("success", true);
            response.put("message", "Leave approved and email sent (system)");
            response.put("emailMethod", "System Email");
            response.put("fromEmail", "system");
            response.put("toEmail", employee.getEmail());
            response.put("hrUser", hrUser.getFullName());
            response.put("leaveRequest", savedLeave);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error in HR approval process: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Error processing HR approval: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // DYNAMIC EMAIL REJECTION: Reject leave with email sent from HR user to Employee
    @PutMapping("/{id}/hr-reject")
    public ResponseEntity<Map<String, Object>> hrRejectLeave(@PathVariable String id, @RequestBody Map<String, String> requestBody, Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        String rejectionReason = requestBody.get("rejectionReason");
        
        try {
            Optional<LeaveRequest> optional = leaveRequestRepository.findById(id);
            if (!optional.isPresent()) {
                response.put("success", false);
                response.put("message", "Leave request not found");
                return ResponseEntity.notFound().build();
            }

            LeaveRequest leave = optional.get();
            leave.setStatus("Rejected");
            leave.setRejectionReason(rejectionReason);
            LeaveRequest savedLeave = leaveRequestRepository.save(leave);

            // Get employee and HR user details
            User employee = findEmployeeForLeaveRequest(leave);
            User hrUser = getCurrentHRUser(authentication);
            
            if (employee == null) {
                response.put("success", false);
                response.put("message", "Employee not found");
                return ResponseEntity.badRequest().body(response);
            }

            if (hrUser == null) {
                response.put("success", false);
                response.put("message", "HR user not found. Please ensure you're logged in as HR.");
                return ResponseEntity.badRequest().body(response);
            }

            System.out.println("📧 Sending rejection email (system)");

            String emailContent = smartEmailTemplateService.generateSmartRejectionEmail(savedLeave, employee, hrUser, rejectionReason);
            emailService.sendHtmlEmail(employee.getEmail(),
                "📋 Leave Request Update - " + savedLeave.getLeaveType(),
                emailContent);

            response.put("success", true);
            response.put("message", "Leave rejected and email sent (system)");
            response.put("emailMethod", "System Email");
            response.put("fromEmail", "system");
            response.put("toEmail", employee.getEmail());
            response.put("hrUser", hrUser.getFullName());
            response.put("rejectionReason", rejectionReason);
            response.put("leaveRequest", savedLeave);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error in HR rejection process: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Error processing HR rejection: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // Helper method to get current HR user
    private User getCurrentHRUser(Authentication authentication) {
        try {
            if (authentication != null) {
                String username = authentication.getName();
                Optional<User> userOpt = userRepository.findByUsername(username);
                return userOpt.orElse(null);
            }
        } catch (Exception e) {
            System.err.println("❌ Error getting HR user: " + e.getMessage());
        }
        return null;
    }

    // Helper method to generate basic approval email
    private String generateBasicApprovalEmail(LeaveRequest leaveRequest, User employee, User hrUser) {
        return String.format("""
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <h2 style="color: #28a745;">✅ Leave Request Approved</h2>
                <p>Dear %s,</p>
                <p>Your leave request has been <strong>approved</strong>.</p>
                <div style="background: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0;">
                    <h3>📅 Leave Details:</h3>
                    <p><strong>Type:</strong> %s</p>
                    <p><strong>Start Date:</strong> %s</p>
                    <p><strong>End Date:</strong> %s</p>
                    <p><strong>Duration:</strong> %d days</p>
                    <p><strong>Reason:</strong> %s</p>
                </div>
                <p>Please ensure proper handover before your leave begins.</p>
                <p>Best regards,<br><strong>%s</strong><br>HR Team</p>
            </div>
            """,
            employee.getFullName(),
            leaveRequest.getLeaveType(),
            leaveRequest.getStartDate(),
            leaveRequest.getEndDate(),
            leaveRequest.getLeaveDuration(),
            leaveRequest.getReason(),
            hrUser != null ? hrUser.getFullName() : "HR Team"
        );
    }

    // Helper method to generate basic rejection email
    private String generateBasicRejectionEmail(LeaveRequest leaveRequest, User employee, User hrUser, String rejectionReason) {
        return String.format("""
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <h2 style="color: #dc3545;">❌ Leave Request Update</h2>
                <p>Dear %s,</p>
                <p>Your leave request has been <strong>declined</strong>.</p>
                <div style="background: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0;">
                    <h3>📅 Leave Details:</h3>
                    <p><strong>Type:</strong> %s</p>
                    <p><strong>Start Date:</strong> %s</p>
                    <p><strong>End Date:</strong> %s</p>
                    <p><strong>Duration:</strong> %d days</p>
                    <p><strong>Your Reason:</strong> %s</p>
                </div>
                <div style="background: #fff3cd; padding: 15px; border-radius: 5px; margin: 20px 0;">
                    <h4>📋 Reason for Decline:</h4>
                    <p>%s</p>
                </div>
                <p>Please feel free to discuss this with HR or submit a revised request.</p>
                <p>Best regards,<br><strong>%s</strong><br>HR Team</p>
            </div>
            """,
            employee.getFullName(),
            leaveRequest.getLeaveType(),
            leaveRequest.getStartDate(),
            leaveRequest.getEndDate(),
            leaveRequest.getLeaveDuration(),
            leaveRequest.getReason(),
            rejectionReason != null ? rejectionReason : "Not specified",
            hrUser != null ? hrUser.getFullName() : "HR Team"
        );
    }

    // DELETE: Delete a leave request by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLeave(@PathVariable String id) {
        if (leaveRequestRepository.existsById(id)) {
            leaveRequestRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    /**
     * Helper method to find employee for leave request using multiple strategies
     */
    private User findEmployeeForLeaveRequest(LeaveRequest leave) {
        User employee = null;
        
        // Strategy 1: Find by employeeId if available
        if (leave.getEmployeeId() != null && !leave.getEmployeeId().isEmpty()) {
            Optional<User> employeeByIdOpt = userRepository.findById(leave.getEmployeeId());
            if (employeeByIdOpt.isPresent()) {
                employee = employeeByIdOpt.get();
                System.out.println("👤 Found employee by ID: " + employee.getFullName() + " (" + employee.getEmail() + ")");
                return employee;
            }
        }
        
        // Strategy 2: Find by fullName
        if (leave.getEmployeeName() != null) {
            Optional<User> employeeByNameOpt = userRepository.findByFullName(leave.getEmployeeName());
            if (employeeByNameOpt.isPresent()) {
                employee = employeeByNameOpt.get();
                System.out.println("👤 Found employee by fullName: " + employee.getFullName() + " (" + employee.getEmail() + ")");
                return employee;
            }
        }
        
        // Strategy 3: Find by username as fallback
        if (leave.getEmployeeName() != null) {
            Optional<User> employeeByUsernameOpt = userRepository.findByUsername(leave.getEmployeeName());
            if (employeeByUsernameOpt.isPresent()) {
                employee = employeeByUsernameOpt.get();
                System.out.println("👤 Found employee by username: " + employee.getFullName() + " (" + employee.getEmail() + ")");
                return employee;
            }
        }
        
        return null;
    }
    
    /**
     * Helper method to log available users for debugging
     */
    private void logAvailableUsers() {
        try {
            List<User> allUsers = userRepository.findAll();
            System.err.println("ℹ️ Available users in database (" + allUsers.size() + "):");
            for (User user : allUsers) {
                System.err.println("  - ID: " + user.getId() + ", Username: " + user.getUsername() + 
                                 ", FullName: " + user.getFullName() + ", Email: " + user.getEmail());
            }
        } catch (Exception e) {
            System.err.println("❌ Error logging available users: " + e.getMessage());
        }
    }
}
