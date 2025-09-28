package com.leavemanagment.leave_app.controller;

import com.leavemanagment.leave_app.model.LeaveRequest;
import com.leavemanagment.leave_app.repository.LeaveRequestRepository;
import com.leavemanagment.leave_app.repository.UserRepository;
import com.leavemanagment.leave_app.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmployeeService employeeService;

    // USER-SPECIFIC STATS: Get dashboard stats for current logged-in user
    @GetMapping("/my-stats")
    public ResponseEntity<Map<String, Object>> getMyDashboardStats(Authentication authentication) {
        try {
            if (authentication == null) {
                System.err.println("❌ No authentication for my-stats request");
                return ResponseEntity.status(401).build();
            }
            
            String username = authentication.getName();
            System.out.println("📊 Getting dashboard stats for user: " + username);
            
            // Get user's full name
            Optional<com.leavemanagment.leave_app.model.User> userOpt = userRepository.findByUsername(username);
            if (!userOpt.isPresent()) {
                System.err.println("❌ User not found: " + username);
                return ResponseEntity.notFound().build();
            }
            
            String fullName = userOpt.get().getFullName();
            System.out.println("👤 Calculating stats for: " + fullName);
            
            Map<String, Object> stats = new HashMap<>();
            
            // Get current year data for this user only
            LocalDate currentDate = LocalDate.now();
            LocalDate yearStart = LocalDate.of(currentDate.getYear(), 1, 1);
            LocalDate yearEnd = LocalDate.of(currentDate.getYear(), 12, 31);
            
            List<LeaveRequest> userRequests = leaveRequestRepository.findByEmployeeName(fullName);
            List<LeaveRequest> currentYearRequests = userRequests.stream()
                .filter(request -> request.getStartDate() != null && 
                        !request.getStartDate().isBefore(yearStart) && 
                        !request.getStartDate().isAfter(yearEnd))
                .collect(Collectors.toList());
            
            // Calculate total leave days taken (approved requests only)
            long totalLeaveDays = currentYearRequests.stream()
                .filter(request -> "Approved".equals(request.getStatus()))
                .mapToLong(LeaveRequest::getLeaveDuration)
                .sum();
            
            // Calculate approval rate for this user
            long totalUserRequests = currentYearRequests.size();
            long approvedUserRequests = currentYearRequests.stream()
                .filter(request -> "Approved".equals(request.getStatus()))
                .count();
            
            double approvalRate = totalUserRequests > 0 ? (double) approvedUserRequests / totalUserRequests * 100 : 0;
            
            // Count pending requests for this user
            long pendingRequests = currentYearRequests.stream()
                .filter(request -> "Pending".equals(request.getStatus()))
                .count();
            
            // Count team members currently on leave (all employees)
            List<LeaveRequest> currentlyOnLeave = leaveRequestRepository.findCurrentlyOnLeave(currentDate);
            
            // Calculate remaining days (assuming 25 days annual leave)
            long remainingDays = Math.max(0, 25 - totalLeaveDays);
            
            stats.put("totalLeaveTaken", totalLeaveDays);
            stats.put("remainingDays", remainingDays);
            stats.put("approvalRate", Math.round(approvalRate));
            stats.put("pendingRequests", pendingRequests);
            stats.put("teamMembersOnLeave", currentlyOnLeave.size());
            
            System.out.println("📈 User stats: " + stats);
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            System.err.println("Error getting user dashboard stats: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // GLOBAL STATS: Get dashboard stats for all users (for HR/Admin)
    @GetMapping("/stats")
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Get current year data
        LocalDate currentDate = LocalDate.now();
        LocalDate yearStart = LocalDate.of(currentDate.getYear(), 1, 1);
        LocalDate yearEnd = LocalDate.of(currentDate.getYear(), 12, 31);
        
        List<LeaveRequest> allRequests = leaveRequestRepository.findAll();
        List<LeaveRequest> currentYearRequests = allRequests.stream()
            .filter(request -> request.getStartDate() != null && 
                    !request.getStartDate().isBefore(yearStart) && 
                    !request.getStartDate().isAfter(yearEnd))
            .collect(Collectors.toList());
        
        // Calculate total leave days taken (approved requests only)
        long totalLeaveDays = currentYearRequests.stream()
            .filter(request -> "Approved".equals(request.getStatus()))
            .mapToLong(LeaveRequest::getLeaveDuration)
            .sum();
        
        // Calculate approval rate
        long totalRequests = currentYearRequests.size();
        long approvedRequests = currentYearRequests.stream()
            .filter(request -> "Approved".equals(request.getStatus()))
            .count();
        
        double approvalRate = totalRequests > 0 ? (double) approvedRequests / totalRequests * 100 : 0;
        
        // Count pending requests
        long pendingRequests = leaveRequestRepository.countByStatus("Pending");
        
        // Count team members currently on leave
        List<LeaveRequest> currentlyOnLeave = leaveRequestRepository.findCurrentlyOnLeave(currentDate);
        
        // Calculate remaining days (assuming 29 days annual leave)
        long remainingDays = Math.max(0, 29 - totalLeaveDays);
        
        stats.put("totalLeaveTaken", totalLeaveDays);
        stats.put("remainingDays", remainingDays);
        stats.put("approvalRate", Math.round(approvalRate));
        stats.put("pendingRequests", pendingRequests);
        stats.put("teamMembersOnLeave", currentlyOnLeave.size());
        
        return stats;
    }

    @GetMapping("/quarterly-data")
    public ResponseEntity<Map<String, Object>> getQuarterlyData(Authentication authentication) {
        try {
            if (authentication == null) {
                System.err.println("❌ No authentication for quarterly-data request");
                return ResponseEntity.status(401).build();
            }
            
            String currentUsername = authentication.getName();
            System.out.println("📊 Loading quarterly data for user: " + currentUsername);
            
            Map<String, Object> data = new HashMap<>();
            
            LocalDate currentDate = LocalDate.now();
            int currentYear = currentDate.getYear();
            
            // Get only the current user's leave requests
            List<LeaveRequest> userRequests = leaveRequestRepository.findByEmployeeName(currentUsername);
            System.out.println("📋 Found " + userRequests.size() + " leave requests for " + currentUsername);
            
            // Calculate quarterly data for current user only
            Map<String, Long> quarterlyTaken = new HashMap<>();
            Map<String, Long> quarterlyRemaining = new HashMap<>();
            
            // Calculate total annual leave allowance (you can make this configurable)
            final long ANNUAL_LEAVE_ALLOWANCE = 25; // 25 days per year
            final long QUARTERLY_ALLOWANCE = ANNUAL_LEAVE_ALLOWANCE / 4; // ~6 days per quarter
            
            for (int quarter = 1; quarter <= 4; quarter++) {
                LocalDate quarterStart = getQuarterStart(currentYear, quarter);
                LocalDate quarterEnd = getQuarterEnd(currentYear, quarter);
                
                long takenDays = userRequests.stream()
                    .filter(request -> "Approved".equals(request.getStatus()))
                    .filter(request -> request.getStartDate() != null && 
                            !request.getStartDate().isBefore(quarterStart) && 
                            !request.getStartDate().isAfter(quarterEnd))
                    .mapToLong(LeaveRequest::getLeaveDuration)
                    .sum();
                
                quarterlyTaken.put("Q" + quarter, takenDays);
                quarterlyRemaining.put("Q" + quarter, Math.max(0, QUARTERLY_ALLOWANCE - takenDays));
                
                System.out.println("Q" + quarter + " - Taken: " + takenDays + ", Remaining: " + (QUARTERLY_ALLOWANCE - takenDays));
            }
            
            // Calculate year-to-date totals
            long totalTakenThisYear = userRequests.stream()
                .filter(request -> "Approved".equals(request.getStatus()))
                .filter(request -> request.getStartDate() != null && 
                        request.getStartDate().getYear() == currentYear)
                .mapToLong(LeaveRequest::getLeaveDuration)
                .sum();
            
            long totalRemainingThisYear = Math.max(0, ANNUAL_LEAVE_ALLOWANCE - totalTakenThisYear);
            
            data.put("taken", quarterlyTaken);
            data.put("remaining", quarterlyRemaining);
            data.put("totalTakenThisYear", totalTakenThisYear);
            data.put("totalRemainingThisYear", totalRemainingThisYear);
            data.put("annualAllowance", ANNUAL_LEAVE_ALLOWANCE);
            
            System.out.println("✅ Quarterly data calculated successfully for " + currentUsername);
            System.out.println("📊 Total taken this year: " + totalTakenThisYear + "/" + ANNUAL_LEAVE_ALLOWANCE);
            
            return ResponseEntity.ok(data);
            
        } catch (Exception e) {
            System.err.println("❌ Error calculating quarterly data: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/upcoming-leaves")
    public List<Map<String, Object>> getUpcomingLeaves() {
        LocalDate currentDate = LocalDate.now();
        List<LeaveRequest> upcomingLeaves = leaveRequestRepository.findUpcomingLeaves(currentDate);
        
        return upcomingLeaves.stream()
            .limit(5) // Show only next 5 upcoming leaves
            .map(leave -> {
                Map<String, Object> leaveInfo = new HashMap<>();
                leaveInfo.put("employeeName", leave.getEmployeeName());
                leaveInfo.put("startDate", leave.getStartDate());
                leaveInfo.put("endDate", leave.getEndDate());
                leaveInfo.put("duration", leave.getLeaveDuration());
                leaveInfo.put("leaveType", leave.getLeaveType());
                leaveInfo.put("status", leave.getStatus());
                return leaveInfo;
            })
            .collect(Collectors.toList());
    }

    @GetMapping("/team-on-leave")
    public List<Map<String, Object>> getTeamMembersOnLeave() {
        LocalDate currentDate = LocalDate.now();
        List<LeaveRequest> currentlyOnLeave = leaveRequestRepository.findCurrentlyOnLeave(currentDate);
        
        return currentlyOnLeave.stream()
            .map(leave -> {
                Map<String, Object> memberInfo = new HashMap<>();
                memberInfo.put("employeeName", leave.getEmployeeName());
                memberInfo.put("startDate", leave.getStartDate());
                memberInfo.put("endDate", leave.getEndDate());
                memberInfo.put("leaveType", leave.getLeaveType());
                memberInfo.put("reason", leave.getReason());
                return memberInfo;
            })
            .collect(Collectors.toList());
    }

    @GetMapping("/notifications")
    public List<Map<String, Object>> getNotifications() {
        List<LeaveRequest> recentRequests = leaveRequestRepository.findTop5ByOrderByIdDesc();
        
        return recentRequests.stream()
            .map(request -> {
                Map<String, Object> notification = new HashMap<>();
                notification.put("id", request.getId());
                notification.put("employeeName", request.getEmployeeName());
                notification.put("status", request.getStatus());
                notification.put("createdAt", request.getCreatedAt());
                notification.put("message", generateNotificationMessage(request));
                return notification;
            })
            .collect(Collectors.toList());
    }

    private String generateNotificationMessage(LeaveRequest request) {
        switch (request.getStatus()) {
            case "Pending":
                return "New leave request submitted";
            case "Approved":
                return "Leave request has been approved";
            case "Rejected":
                return "Leave request has been rejected";
            default:
                return "Leave request updated";
        }
    }

    private LocalDate getQuarterStart(int year, int quarter) {
        switch (quarter) {
            case 1: return LocalDate.of(year, Month.JANUARY, 1);
            case 2: return LocalDate.of(year, Month.APRIL, 1);
            case 3: return LocalDate.of(year, Month.JULY, 1);
            case 4: return LocalDate.of(year, Month.OCTOBER, 1);
            default: throw new IllegalArgumentException("Invalid quarter: " + quarter);
        }
    }

    private LocalDate getQuarterEnd(int year, int quarter) {
        switch (quarter) {
            case 1: return LocalDate.of(year, Month.MARCH, 31);
            case 2: return LocalDate.of(year, Month.JUNE, 30);
            case 3: return LocalDate.of(year, Month.SEPTEMBER, 30);
            case 4: return LocalDate.of(year, Month.DECEMBER, 31);
            default: throw new IllegalArgumentException("Invalid quarter: " + quarter);
        }
    }
    
    // ========== HR-SPECIFIC ENDPOINTS ==========
    
    @GetMapping("/hr/employee-stats")
    public Map<String, Object> getHREmployeeStats() {
        EmployeeService.EmployeeStats stats = employeeService.getEmployeeStats();
        
        Map<String, Object> hrStats = new HashMap<>();
        hrStats.put("totalEmployees", stats.getTotalEmployees());
        hrStats.put("employeesOnLeave", stats.getEmployeesOnLeave());
        hrStats.put("employeesPresent", stats.getEmployeesPresent());
        
        // Additional HR metrics
        long pendingRequests = leaveRequestRepository.countByStatus("Pending");
        long totalRequests = leaveRequestRepository.count();
        long approvedRequests = leaveRequestRepository.countByStatus("Approved");
        long rejectedRequests = leaveRequestRepository.countByStatus("Rejected");
        
        hrStats.put("pendingApprovals", pendingRequests);
        hrStats.put("totalRequests", totalRequests);
        hrStats.put("approvedRequests", approvedRequests);
        hrStats.put("rejectedRequests", rejectedRequests);
        
        return hrStats;
    }
    
    @GetMapping("/hr/pending-requests")
    public List<Map<String, Object>> getPendingRequests() {
        System.out.println("🔍 HR: Fetching pending requests...");
        List<LeaveRequest> pendingRequests = leaveRequestRepository.findByStatus("Pending");
        System.out.println("📋 HR: Found " + pendingRequests.size() + " pending requests");
        
        List<Map<String, Object>> result = pendingRequests.stream()
            .map(request -> {
                Map<String, Object> requestInfo = new HashMap<>();
                requestInfo.put("id", request.getId());
                requestInfo.put("employeeName", request.getEmployeeName());
                requestInfo.put("startDate", request.getStartDate());
                requestInfo.put("endDate", request.getEndDate());
                requestInfo.put("duration", request.getLeaveDuration());
                requestInfo.put("leaveType", request.getLeaveType());
                requestInfo.put("reason", request.getReason());
                requestInfo.put("status", request.getStatus());
                requestInfo.put("rejectionReason", request.getRejectionReason());
                requestInfo.put("createdAt", request.getCreatedAt());
                return requestInfo;
            })
            .collect(Collectors.toList());
        
        System.out.println("✅ HR: Returning " + result.size() + " pending requests");
        return result;
    }
    
    @GetMapping("/hr/all-requests")
    public List<Map<String, Object>> getAllRequests() {
        System.out.println("🔍 HR: Fetching all requests...");
        List<LeaveRequest> allRequests = leaveRequestRepository.findAll();
        System.out.println("📋 HR: Found " + allRequests.size() + " total requests");
        
        List<Map<String, Object>> result = allRequests.stream()
            .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt())) // Latest first
            .map(request -> {
                Map<String, Object> requestInfo = new HashMap<>();
                requestInfo.put("id", request.getId());
                requestInfo.put("employeeName", request.getEmployeeName());
                requestInfo.put("startDate", request.getStartDate());
                requestInfo.put("endDate", request.getEndDate());
                requestInfo.put("duration", request.getLeaveDuration());
                requestInfo.put("leaveType", request.getLeaveType());
                requestInfo.put("reason", request.getReason());
                requestInfo.put("status", request.getStatus());
                requestInfo.put("rejectionReason", request.getRejectionReason());
                requestInfo.put("createdAt", request.getCreatedAt());
                return requestInfo;
            })
            .collect(Collectors.toList());
        
        System.out.println("✅ HR: Returning " + result.size() + " all requests");
        return result;
    }
    
    @GetMapping("/hr/department-stats")
    public Map<String, Object> getDepartmentStats() {
        List<LeaveRequest> allRequests = leaveRequestRepository.findAll();
        
        // Calculate real department statistics from actual leave requests
        Map<String, Long> departmentLeaves = new HashMap<>();
        
        // Get all employees to map names to departments
        List<com.leavemanagment.leave_app.model.Employee> allEmployees = employeeService.getAllActiveEmployees();
        Map<String, String> employeeToDepartment = new HashMap<>();
        
        for (com.leavemanagment.leave_app.model.Employee employee : allEmployees) {
            employeeToDepartment.put(employee.getFullName(), employee.getDepartment());
        }
        
        for (LeaveRequest request : allRequests) {
            if ("Approved".equals(request.getStatus()) && request.getStartDate() != null && request.getEndDate() != null) {
                String employeeName = request.getEmployeeName();
                long days = request.getLeaveDuration();
                
                // Get department from our mapping
                String department = employeeToDepartment.get(employeeName);
                if (department != null && !department.trim().isEmpty()) {
                    departmentLeaves.merge(department, days, Long::sum);
                }
            }
        }
        
        // If no real data, provide empty map
        if (departmentLeaves.isEmpty()) {
            departmentLeaves.put("No Data", 0L);
        }
        
        Map<String, Object> deptStats = new HashMap<>();
        deptStats.put("departmentLeaves", departmentLeaves);
        
        return deptStats;
    }
}