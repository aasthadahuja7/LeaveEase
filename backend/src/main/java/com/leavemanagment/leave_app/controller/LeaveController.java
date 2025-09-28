package com.leavemanagment.leave_app.controller;

import com.leavemanagment.leave_app.model.LeaveRequest;
import com.leavemanagment.leave_app.model.User;
import com.leavemanagment.leave_app.repository.LeaveRequestRepository;
import com.leavemanagment.leave_app.repository.UserRepository;
import com.leavemanagment.leave_app.service.AIEmailGeneratorService;
import com.leavemanagment.leave_app.service.AINotificationService;
import com.leavemanagment.leave_app.service.DynamicEmailService;
import com.leavemanagment.leave_app.service.EmailService;
import com.leavemanagment.leave_app.service.EmployeeEmailService;
import com.leavemanagment.leave_app.service.HREmailService;
import com.leavemanagment.leave_app.service.SmartEmailTemplateService;
import com.leavemanagment.leave_app.service.UserEmailConfigService;
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


@RestController
@RequestMapping("/leaves")
public class LeaveController {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AINotificationService aiNotificationService;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private EmployeeEmailService employeeEmailService;
    
    @Autowired
    private HREmailService hrEmailService;
    
    @Autowired
    private AIEmailGeneratorService aiEmailGeneratorService;
    
    @Autowired
    private SmartEmailTemplateService smartEmailTemplateService;
    
    @Autowired
    private DynamicEmailService dynamicEmailService;
    
    @Autowired
    private UserEmailConfigService userEmailConfigService;

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
            Optional<com.leavemanagment.leave_app.model.User> userOpt = userRepository.findByUsername(username);
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
            
            // Send notification through N8N workflow with fallback to direct email
            try {
                User employee = findEmployeeForLeaveRequest(leave);
                if (employee != null) {
                    System.out.println("🤖 Sending approval notification via N8N workflow to: " + employee.getEmail());
                    
                    // Use N8N workflow for notification
                    aiNotificationService.sendLeaveApprovedNotification(savedLeave, employee);
                    
                    // Also send HR email as backup/additional notification
                    hrEmailService.sendLeaveApprovedEmailFromHR(savedLeave, employee, authentication);
                    
                    System.out.println("✅ Approval notifications sent successfully to " + employee.getFullName() + " (" + employee.getEmail() + ")");
                } else {
                    System.err.println("❌ Employee not found for leave request: " + leave.getEmployeeName());
                    logAvailableUsers();
                }
            } catch (Exception e) {
                System.err.println("❌ Error sending approval notifications: " + e.getMessage());
                e.printStackTrace();
            }
            
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
                
                // Send notification through N8N workflow with fallback to direct email
                try {
                    User employee = findEmployeeForLeaveRequest(leave);
                    if (employee != null) {
                        System.out.println("🤖 Sending rejection notification via N8N workflow to: " + employee.getEmail());
                        
                        // Use N8N workflow for notification
                        aiNotificationService.sendLeaveRejectedNotification(savedLeave, employee, rejectionReason);
                        
                        // Also send HR email as backup/additional notification
                        hrEmailService.sendLeaveRejectedEmailFromHR(savedLeave, employee, rejectionReason, authentication);
                        
                        System.out.println("✅ Rejection notifications sent successfully to " + employee.getFullName() + " (" + employee.getEmail() + ")");
                    } else {
                        System.err.println("❌ Employee not found for leave request: " + leave.getEmployeeName());
                        logAvailableUsers();
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error sending rejection notifications: " + e.getMessage());
                    e.printStackTrace();
                }
                
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
                
                // Send approval notification via N8N workflow
                try {
                    Optional<User> employeeOpt = userRepository.findByFullName(leave.getEmployeeName());
                    if (employeeOpt.isPresent()) {
                        User employee = employeeOpt.get();
                        System.out.println("🤖 Sending approval notification via N8N workflow to: " + employee.getEmail());
                        
                        // Use N8N workflow for notification
                        aiNotificationService.sendLeaveApprovedNotification(leave, employee);
                        
                        // Also send direct email as backup
                        emailService.sendLeaveApprovedEmail(leave, employee);
                    } else {
                        // Try to find by username as fallback
                        Optional<User> employeeByUsername = userRepository.findByUsername(leave.getEmployeeName());
                        if (employeeByUsername.isPresent()) {
                            User employee = employeeByUsername.get();
                            System.out.println("🤖 Sending approval notification via N8N workflow to: " + employee.getEmail() + " (found by username)");
                            
                            // Use N8N workflow for notification
                            aiNotificationService.sendLeaveApprovedNotification(leave, employee);
                            
                            // Also send direct email as backup
                            emailService.sendLeaveApprovedEmail(leave, employee);
                        } else {
                            System.err.println("❌ Employee not found for notification: " + leave.getEmployeeName());
                        }
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error sending approval notification: " + e.getMessage());
                }
                
            } else if ("reject".equalsIgnoreCase(action)) {
                leave.setStatus("Rejected");
                leave.setRejectionReason(reason);
                
                // Send rejection notification via N8N workflow
                try {
                    Optional<User> employeeOpt = userRepository.findByFullName(leave.getEmployeeName());
                    if (employeeOpt.isPresent()) {
                        User employee = employeeOpt.get();
                        System.out.println("🤖 Sending rejection notification via N8N workflow to: " + employee.getEmail());
                        
                        // Use N8N workflow for notification
                        aiNotificationService.sendLeaveRejectedNotification(leave, employee, reason);
                        
                        // Also send direct email as backup
                        emailService.sendLeaveRejectedEmail(leave, employee, reason);
                    } else {
                        // Try to find by username as fallback
                        Optional<User> employeeByUsername = userRepository.findByUsername(leave.getEmployeeName());
                        if (employeeByUsername.isPresent()) {
                            User employee = employeeByUsername.get();
                            System.out.println("🤖 Sending rejection notification via N8N workflow to: " + employee.getEmail() + " (found by username)");
                            
                            // Use N8N workflow for notification
                            aiNotificationService.sendLeaveRejectedNotification(leave, employee, reason);
                            
                            // Also send direct email as backup
                            emailService.sendLeaveRejectedEmail(leave, employee, reason);
                        } else {
                            System.err.println("❌ Employee not found for notification: " + leave.getEmployeeName());
                        }
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error sending rejection notification: " + e.getMessage());
                }
                
            } else {
                return ResponseEntity.badRequest().build();
            }
            
            return ResponseEntity.ok(leaveRequestRepository.save(leave));
        }
        return ResponseEntity.notFound().build();
    }


    // AI-POWERED APPROVAL: Approve leave with AI-generated email
    @PutMapping("/{id}/ai-approve")
    public ResponseEntity<Map<String, Object>> aiApproveLeave(@PathVariable String id, Authentication authentication) {
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

            System.out.println("🤖 Generating AI-powered approval email for: " + employee.getEmail());

            // Try different AI approaches in order of preference
            String emailContent = null;
            String aiMethod = "fallback";

            // Option 1: Try OpenAI (if API key is configured)
            try {
                emailContent = aiEmailGeneratorService.generateApprovalEmail(savedLeave, employee, hrUser);
                if (emailContent != null && !emailContent.trim().isEmpty()) {
                    aiMethod = "OpenAI";
                }
            } catch (Exception e) {
                System.out.println("⚠️ OpenAI not available, trying next option...");
            }

            // Option 2: Try Smart Template Engine (always available)
            if (emailContent == null) {
                try {
                    emailContent = smartEmailTemplateService.generateSmartApprovalEmail(savedLeave, employee, hrUser);
                    aiMethod = "Smart Template";
                } catch (Exception e) {
                    System.err.println("❌ Smart template failed: " + e.getMessage());
                }
            }

            // Option 3: Fallback to basic template
            if (emailContent == null) {
                emailContent = generateBasicApprovalEmail(savedLeave, employee, hrUser);
                aiMethod = "Basic Template";
            }

            // Send the email
            try {
                emailService.sendHtmlEmail(employee.getEmail(), 
                    "✅ Leave Request Approved - " + savedLeave.getLeaveType(), 
                    emailContent);
                
                System.out.println("✅ AI-powered approval email sent successfully using: " + aiMethod);
                
                response.put("success", true);
                response.put("message", "Leave approved and AI-powered email sent successfully");
                response.put("aiMethod", aiMethod);
                response.put("employeeEmail", employee.getEmail());
                response.put("leaveRequest", savedLeave);
                
                return ResponseEntity.ok(response);
                
            } catch (Exception e) {
                System.err.println("❌ Error sending AI email: " + e.getMessage());
                response.put("success", false);
                response.put("message", "Leave approved but email failed: " + e.getMessage());
                return ResponseEntity.status(500).body(response);
            }

        } catch (Exception e) {
            System.err.println("❌ Error in AI approval process: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Error processing AI approval: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // AI-POWERED REJECTION: Reject leave with AI-generated email
    @PutMapping("/{id}/ai-reject")
    public ResponseEntity<Map<String, Object>> aiRejectLeave(
            @PathVariable String id, 
            @RequestBody(required = false) Map<String, String> requestBody, 
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<LeaveRequest> optional = leaveRequestRepository.findById(id);
            if (!optional.isPresent()) {
                response.put("success", false);
                response.put("message", "Leave request not found");
                return ResponseEntity.notFound().build();
            }

            LeaveRequest leave = optional.get();
            String rejectionReason = null;
            if (requestBody != null && requestBody.containsKey("rejectionReason")) {
                rejectionReason = requestBody.get("rejectionReason");
            }

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

            System.out.println("🤖 Generating AI-powered rejection email for: " + employee.getEmail());

            // Try different AI approaches in order of preference
            String emailContent = null;
            String aiMethod = "fallback";

            // Option 1: Try OpenAI (if API key is configured)
            try {
                emailContent = aiEmailGeneratorService.generateRejectionEmail(savedLeave, employee, hrUser, rejectionReason);
                if (emailContent != null && !emailContent.trim().isEmpty()) {
                    aiMethod = "OpenAI";
                }
            } catch (Exception e) {
                System.out.println("⚠️ OpenAI not available, trying next option...");
            }

            // Option 2: Try Smart Template Engine (always available)
            if (emailContent == null) {
                try {
                    emailContent = smartEmailTemplateService.generateSmartRejectionEmail(savedLeave, employee, hrUser, rejectionReason);
                    aiMethod = "Smart Template";
                } catch (Exception e) {
                    System.err.println("❌ Smart template failed: " + e.getMessage());
                }
            }

            // Option 3: Fallback to basic template
            if (emailContent == null) {
                emailContent = generateBasicRejectionEmail(savedLeave, employee, hrUser, rejectionReason);
                aiMethod = "Basic Template";
            }

            // Send the email
            try {
                emailService.sendHtmlEmail(employee.getEmail(), 
                    "📋 Leave Request Update - " + savedLeave.getLeaveType(), 
                    emailContent);
                
                System.out.println("✅ AI-powered rejection email sent successfully using: " + aiMethod);
                
                response.put("success", true);
                response.put("message", "Leave rejected and AI-powered email sent successfully");
                response.put("aiMethod", aiMethod);
                response.put("employeeEmail", employee.getEmail());
                response.put("rejectionReason", rejectionReason);
                response.put("leaveRequest", savedLeave);
                
                return ResponseEntity.ok(response);
                
            } catch (Exception e) {
                System.err.println("❌ Error sending AI email: " + e.getMessage());
                response.put("success", false);
                response.put("message", "Leave rejected but email failed: " + e.getMessage());
                return ResponseEntity.status(500).body(response);
            }

        } catch (Exception e) {
            System.err.println("❌ Error in AI rejection process: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Error processing AI rejection: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

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

            System.out.println("📧 Sending approval email from HR (" + hrUser.getEmail() + ") to Employee (" + employee.getEmail() + ")");

            // Check if HR user has email configuration
            String hrEmailPassword = userEmailConfigService.getUserEmailPassword(hrUser.getEmail());
            
            if (hrEmailPassword != null && !hrEmailPassword.equals("PLACEHOLDER_APP_PASSWORD")) {
                // Send email from HR user to Employee
                try {
                    dynamicEmailService.sendApprovalEmailFromHR(savedLeave, employee, hrUser, hrEmailPassword);
                    
                    response.put("success", true);
                    response.put("message", "Leave approved and email sent from HR to Employee");
                    response.put("emailMethod", "HR Direct Email");
                    response.put("fromEmail", hrUser.getEmail());
                    response.put("toEmail", employee.getEmail());
                    response.put("hrUser", hrUser.getFullName());
                    response.put("leaveRequest", savedLeave);
                    
                    return ResponseEntity.ok(response);
                    
                } catch (Exception e) {
                    System.err.println("❌ Error sending HR email: " + e.getMessage());
                    // Fallback to system email
                }
            }
            
            // Fallback: Use AI-powered system email
            System.out.println("📧 HR email not configured, falling back to AI-powered system email");
            
            String emailContent = smartEmailTemplateService.generateSmartApprovalEmail(savedLeave, employee, hrUser);
            emailService.sendHtmlEmail(employee.getEmail(), 
                "✅ Leave Request Approved - " + savedLeave.getLeaveType(), 
                emailContent);
            
            response.put("success", true);
            response.put("message", "Leave approved and email sent (system fallback)");
            response.put("emailMethod", "System Email (AI-powered)");
            response.put("fromEmail", "system");
            response.put("toEmail", employee.getEmail());
            response.put("hrUser", hrUser.getFullName());
            response.put("note", "Configure HR email credentials for direct HR-to-Employee communication");
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

            System.out.println("📧 Sending rejection email from HR (" + hrUser.getEmail() + ") to Employee (" + employee.getEmail() + ")");

            // Check if HR user has email configuration
            String hrEmailPassword = userEmailConfigService.getUserEmailPassword(hrUser.getEmail());
            
            if (hrEmailPassword != null && !hrEmailPassword.equals("PLACEHOLDER_APP_PASSWORD")) {
                // Send email from HR user to Employee
                try {
                    dynamicEmailService.sendRejectionEmailFromHR(savedLeave, employee, hrUser, hrEmailPassword, rejectionReason);
                    
                    response.put("success", true);
                    response.put("message", "Leave rejected and email sent from HR to Employee");
                    response.put("emailMethod", "HR Direct Email");
                    response.put("fromEmail", hrUser.getEmail());
                    response.put("toEmail", employee.getEmail());
                    response.put("hrUser", hrUser.getFullName());
                    response.put("rejectionReason", rejectionReason);
                    response.put("leaveRequest", savedLeave);
                    
                    return ResponseEntity.ok(response);
                    
                } catch (Exception e) {
                    System.err.println("❌ Error sending HR email: " + e.getMessage());
                    // Fallback to system email
                }
            }
            
            // Fallback: Use AI-powered system email
            System.out.println("📧 HR email not configured, falling back to AI-powered system email");
            
            String emailContent = smartEmailTemplateService.generateSmartRejectionEmail(savedLeave, employee, hrUser, rejectionReason);
            emailService.sendHtmlEmail(employee.getEmail(), 
                "📋 Leave Request Update - " + savedLeave.getLeaveType(), 
                emailContent);
            
            response.put("success", true);
            response.put("message", "Leave rejected and email sent (system fallback)");
            response.put("emailMethod", "System Email (AI-powered)");
            response.put("fromEmail", "system");
            response.put("toEmail", employee.getEmail());
            response.put("hrUser", hrUser.getFullName());
            response.put("rejectionReason", rejectionReason);
            response.put("note", "Configure HR email credentials for direct HR-to-Employee communication");
            response.put("leaveRequest", savedLeave);
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error in HR rejection process: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Error processing HR rejection: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // HR EMAIL CONFIGURATION: Set HR user's email credentials
    @PostMapping("/hr-email-config")
    public ResponseEntity<Map<String, Object>> configureHREmail(@RequestBody Map<String, String> request, Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User hrUser = getCurrentHRUser(authentication);
            if (hrUser == null) {
                response.put("success", false);
                response.put("message", "HR user not found. Please ensure you're logged in as HR.");
                return ResponseEntity.badRequest().body(response);
            }
            
            String appPassword = request.get("appPassword");
            if (appPassword == null || appPassword.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Please provide appPassword (Gmail App Password)");
                response.put("instructions", "Generate an App Password from your Gmail security settings");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Store the HR user's email configuration in database (persistent)
            userEmailConfigService.setUserEmailPassword(hrUser.getEmail(), appPassword);
            
            response.put("success", true);
            response.put("message", "HR email configuration saved successfully and will persist across sessions");
            response.put("hrEmail", hrUser.getEmail());
            response.put("hrUser", hrUser.getFullName());
            response.put("configured", true);
            response.put("note", "You can now send emails directly from your email to employees");
            
            System.out.println("✅ HR email configuration saved persistently for: " + hrUser.getEmail());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error configuring HR email: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Error configuring HR email: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    // GET HR EMAIL CONFIGURATION STATUS: Check if HR email is configured
    @GetMapping("/hr-email-config/status")
    public ResponseEntity<Map<String, Object>> getHREmailConfigStatus(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User hrUser = getCurrentHRUser(authentication);
            if (hrUser == null) {
                response.put("success", false);
                response.put("message", "HR user not found. Please ensure you're logged in as HR.");
                return ResponseEntity.badRequest().body(response);
            }

            boolean hasConfig = userEmailConfigService.hasEmailConfig(hrUser.getEmail());
            String appPassword = userEmailConfigService.getUserEmailPassword(hrUser.getEmail());
            
            response.put("success", true);
            response.put("hrEmail", hrUser.getEmail());
            response.put("hrUser", hrUser.getFullName());
            response.put("configured", hasConfig);
            response.put("hasValidPassword", appPassword != null && !appPassword.equals("PLACEHOLDER_APP_PASSWORD"));
            
            if (hasConfig && appPassword != null && !appPassword.equals("PLACEHOLDER_APP_PASSWORD")) {
                response.put("message", "HR email is configured and ready for direct communication");
                response.put("status", "configured");
            } else {
                response.put("message", "HR email not configured - using system fallback");
                response.put("status", "not_configured");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error checking HR email config status: " + e.getMessage());
            e.printStackTrace();
            
            response.put("success", false);
            response.put("message", "Error checking configuration: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // TEST EMAIL CONFIGURATION: Test endpoint to verify email sending
    @PostMapping("/test-email")
    public ResponseEntity<Map<String, Object>> testEmailConfiguration(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String testEmail = request.get("testEmail");
            if (testEmail == null || testEmail.isEmpty()) {
                response.put("success", false);
                response.put("message", "Please provide testEmail in request body");
                return ResponseEntity.badRequest().body(response);
            }
            
            System.out.println("📧 Testing email configuration with: " + testEmail);
            
            String testEmailContent = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #28a745;">✅ Email Configuration Test</h2>
                    <p>Congratulations! Your LeaveEase email system is working perfectly!</p>
                    <div style="background: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0;">
                        <h3>🤖 AI Email Features Active:</h3>
                        <ul>
                            <li>✅ Smart Template Engine</li>
                            <li>✅ Personalized Content Generation</li>
                            <li>✅ Professional HTML Formatting</li>
                            <li>✅ Mobile-Responsive Design</li>
                            <li>✅ Multi-level Fallback System</li>
                        </ul>
                    </div>
                    <p>Your employees will now receive beautiful, AI-generated email notifications for:</p>
                    <ul>
                        <li>🎉 Leave Approvals</li>
                        <li>📋 Leave Rejections</li>
                        <li>📅 Status Updates</li>
                    </ul>
                    <p style="color: #666; font-size: 12px; margin-top: 30px;">
                        This is a test email from LeaveEase AI Email System
                    </p>
                </div>
                """;
            
            emailService.sendHtmlEmail(testEmail, "✅ LeaveEase Email Test - Configuration Successful!", testEmailContent);
            
            response.put("success", true);
            response.put("message", "Test email sent successfully!");
            response.put("testEmail", testEmail);
            response.put("note", "Check your inbox (and spam folder) for the test email");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Email test failed: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Email test failed: " + e.getMessage());
            response.put("troubleshooting", "Check your email configuration in application.properties");
            return ResponseEntity.status(500).body(response);
        }
    }

    // TEST N8N CONNECTION: Test endpoint to verify N8N webhook connectivity
    @GetMapping("/test-n8n")
    public ResponseEntity<Map<String, Object>> testN8NConnection() {
        Map<String, Object> response = new HashMap<>();
        try {
            System.out.println("🧪 Testing N8N connection...");
            boolean isConnected = aiNotificationService.testN8NConnection();
            
            response.put("success", isConnected);
            response.put("message", isConnected ? "N8N connection successful" : "N8N connection failed");
            response.put("timestamp", LocalDateTime.now());
            
            if (isConnected) {
                System.out.println("✅ N8N connection test passed");
                return ResponseEntity.ok(response);
            } else {
                System.out.println("❌ N8N connection test failed");
                return ResponseEntity.status(503).body(response);
            }
        } catch (Exception e) {
            System.err.println("❌ Error testing N8N connection: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Error testing N8N connection: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
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
