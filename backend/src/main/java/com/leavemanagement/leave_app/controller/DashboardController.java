package com.leavemanagement.leave_app.controller;

import com.leavemanagement.leave_app.model.LeaveRequest;
import com.leavemanagement.leave_app.model.User;
import com.leavemanagement.leave_app.repository.LeaveRequestRepository;
import com.leavemanagement.leave_app.repository.UserRepository;
import com.leavemanagement.leave_app.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    private static final long ANNUAL_LEAVE_ALLOWANCE = 25L;
    private static final long QUARTERLY_ALLOWANCE = ANNUAL_LEAVE_ALLOWANCE / 4;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeService employeeService;

    // User Dashboard APIs
    @GetMapping("/my-stats")
    public ResponseEntity<Map<String, Object>> getMyDashboardStats(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        String username = authentication.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String fullName = userOpt.get().getFullName();
        List<LeaveRequest> userRequests = leaveRequestRepository.findByEmployeeName(fullName);
        LocalDate currentDate = LocalDate.now();

        return ResponseEntity.ok(buildDashboardStats(userRequests, currentDate, ANNUAL_LEAVE_ALLOWANCE));
    }

    @GetMapping("/stats")
    public Map<String, Object> getDashboardStats() {
        LocalDate currentDate = LocalDate.now();
        List<LeaveRequest> allRequests = leaveRequestRepository.findAll();
        return buildDashboardStats(allRequests, currentDate, 29L);
    }

    @GetMapping("/quarterly-data")
    public ResponseEntity<Map<String, Object>> getQuarterlyData(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        String currentUsername = authentication.getName();
        LocalDate currentDate = LocalDate.now();
        List<LeaveRequest> userRequests = leaveRequestRepository.findByEmployeeName(currentUsername);

        Map<String, Object> data = new HashMap<>();
        Map<String, Long> quarterlyTaken = new HashMap<>();
        Map<String, Long> quarterlyRemaining = new HashMap<>();

        for (int quarter = 1; quarter <= 4; quarter++) {
            LocalDate quarterStart = getQuarterStart(currentDate.getYear(), quarter);
            LocalDate quarterEnd = getQuarterEnd(currentDate.getYear(), quarter);

            long takenDays = userRequests.stream()
                    .filter(request -> "Approved".equals(request.getStatus()))
                    .filter(request -> request.getStartDate() != null
                            && !request.getStartDate().isBefore(quarterStart)
                            && !request.getStartDate().isAfter(quarterEnd))
                    .mapToLong(LeaveRequest::getLeaveDuration)
                    .sum();

            quarterlyTaken.put("Q" + quarter, takenDays);
            quarterlyRemaining.put("Q" + quarter, Math.max(0, QUARTERLY_ALLOWANCE - takenDays));
        }

        long totalTakenThisYear = userRequests.stream()
                .filter(request -> "Approved".equals(request.getStatus()))
                .filter(request -> request.getStartDate() != null && request.getStartDate().getYear() == currentDate.getYear())
                .mapToLong(LeaveRequest::getLeaveDuration)
                .sum();

        data.put("taken", quarterlyTaken);
        data.put("remaining", quarterlyRemaining);
        data.put("totalTakenThisYear", totalTakenThisYear);
        data.put("totalRemainingThisYear", Math.max(0, ANNUAL_LEAVE_ALLOWANCE - totalTakenThisYear));
        data.put("annualAllowance", ANNUAL_LEAVE_ALLOWANCE);

        return ResponseEntity.ok(data);
    }

    @GetMapping("/upcoming-leaves")
    public List<Map<String, Object>> getUpcomingLeaves() {
        return leaveRequestRepository.findUpcomingLeaves(LocalDate.now()).stream()
                .limit(5)
                .map(this::mapUpcomingLeave)
                .collect(Collectors.toList());
    }

    @GetMapping("/team-on-leave")
    public List<Map<String, Object>> getTeamMembersOnLeave() {
        return leaveRequestRepository.findCurrentlyOnLeave(LocalDate.now()).stream()
                .map(this::mapTeamMemberOnLeave)
                .collect(Collectors.toList());
    }

    // HR Dashboard APIs
    @GetMapping("/hr/employee-stats")
    public Map<String, Object> getHREmployeeStats() {
        EmployeeService.EmployeeStats stats = employeeService.getEmployeeStats();
        Map<String, Object> hrStats = new HashMap<>();

        hrStats.put("totalEmployees", stats.getTotalEmployees());
        hrStats.put("employeesOnLeave", stats.getEmployeesOnLeave());
        hrStats.put("employeesPresent", stats.getEmployeesPresent());
        hrStats.put("pendingApprovals", leaveRequestRepository.countByStatus("Pending"));
        hrStats.put("totalRequests", leaveRequestRepository.count());
        hrStats.put("approvedRequests", leaveRequestRepository.countByStatus("Approved"));
        hrStats.put("rejectedRequests", leaveRequestRepository.countByStatus("Rejected"));

        return hrStats;
    }

    @GetMapping("/hr/pending-requests")
    public List<Map<String, Object>> getPendingRequests() {
        return mapLeaveRequests(leaveRequestRepository.findByStatus("Pending"));
    }

    @GetMapping("/hr/all-requests")
    public List<Map<String, Object>> getAllRequests() {
        return leaveRequestRepository.findAll().stream()
                .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                .map(this::mapLeaveRequestForHR)
                .collect(Collectors.toList());
    }

    @GetMapping("/hr/department-stats")
    public Map<String, Object> getDepartmentStats() {
        List<LeaveRequest> allRequests = leaveRequestRepository.findAll();
        Map<String, String> employeeToDepartment = employeeService.getAllActiveEmployees().stream()
                .collect(Collectors.toMap(com.leavemanagement.leave_app.model.Employee::getFullName,
                        com.leavemanagement.leave_app.model.Employee::getDepartment,
                        (existing, replacement) -> existing));

        Map<String, Long> departmentLeaves = new HashMap<>();

        for (LeaveRequest request : allRequests) {
            if ("Approved".equals(request.getStatus()) && request.getStartDate() != null && request.getEndDate() != null) {
                String department = employeeToDepartment.get(request.getEmployeeName());
                if (department != null && !department.trim().isEmpty()) {
                    departmentLeaves.merge(department, request.getLeaveDuration(), Long::sum);
                }
            }
        }

        if (departmentLeaves.isEmpty()) {
            departmentLeaves.put("No Data", 0L);
        }

        Map<String, Object> deptStats = new HashMap<>();
        deptStats.put("departmentLeaves", departmentLeaves);
        return deptStats;
    }

    // Notification APIs
    @GetMapping("/notifications")
    public List<Map<String, Object>> getNotifications() {
        return leaveRequestRepository.findTop5ByOrderByIdDesc().stream()
                .map(this::mapNotification)
                .collect(Collectors.toList());
    }

    // Helper Methods
    private Map<String, Object> buildDashboardStats(List<LeaveRequest> requests, LocalDate currentDate, long annualLeaveAllowance) {
        LocalDate yearStart = LocalDate.of(currentDate.getYear(), 1, 1);
        LocalDate yearEnd = LocalDate.of(currentDate.getYear(), 12, 31);

        List<LeaveRequest> currentYearRequests = requests.stream()
                .filter(request -> request.getStartDate() != null
                        && !request.getStartDate().isBefore(yearStart)
                        && !request.getStartDate().isAfter(yearEnd))
                .collect(Collectors.toList());

        long totalLeaveDays = calculateApprovedLeaveDays(currentYearRequests);
        long pendingRequests = currentYearRequests.stream()
                .filter(request -> "Pending".equals(request.getStatus()))
                .count();
        long totalRequests = currentYearRequests.size();
        long approvedRequests = currentYearRequests.stream()
                .filter(request -> "Approved".equals(request.getStatus()))
                .count();
        long approvalRate = totalRequests > 0 ? Math.round((double) approvedRequests / totalRequests * 100) : 0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalLeaveTaken", totalLeaveDays);
        stats.put("remainingDays", Math.max(0, annualLeaveAllowance - totalLeaveDays));
        stats.put("approvalRate", approvalRate);
        stats.put("pendingRequests", pendingRequests);
        stats.put("teamMembersOnLeave", leaveRequestRepository.findCurrentlyOnLeave(currentDate).size());
        return stats;
    }

    private long calculateApprovedLeaveDays(List<LeaveRequest> requests) {
        return requests.stream()
                .filter(request -> "Approved".equals(request.getStatus()))
                .mapToLong(LeaveRequest::getLeaveDuration)
                .sum();
    }

    private LocalDate getQuarterStart(int year, int quarter) {
        switch (quarter) {
            case 1:
                return LocalDate.of(year, Month.JANUARY, 1);
            case 2:
                return LocalDate.of(year, Month.APRIL, 1);
            case 3:
                return LocalDate.of(year, Month.JULY, 1);
            case 4:
                return LocalDate.of(year, Month.OCTOBER, 1);
            default:
                throw new IllegalArgumentException("Invalid quarter: " + quarter);
        }
    }

    private LocalDate getQuarterEnd(int year, int quarter) {
        switch (quarter) {
            case 1:
                return LocalDate.of(year, Month.MARCH, 31);
            case 2:
                return LocalDate.of(year, Month.JUNE, 30);
            case 3:
                return LocalDate.of(year, Month.SEPTEMBER, 30);
            case 4:
                return LocalDate.of(year, Month.DECEMBER, 31);
            default:
                throw new IllegalArgumentException("Invalid quarter: " + quarter);
        }
    }

    private Map<String, Object> mapUpcomingLeave(LeaveRequest leave) {
        Map<String, Object> leaveInfo = new HashMap<>();
        leaveInfo.put("employeeName", leave.getEmployeeName());
        leaveInfo.put("startDate", leave.getStartDate());
        leaveInfo.put("endDate", leave.getEndDate());
        leaveInfo.put("duration", leave.getLeaveDuration());
        leaveInfo.put("leaveType", leave.getLeaveType());
        leaveInfo.put("status", leave.getStatus());
        return leaveInfo;
    }

    private Map<String, Object> mapTeamMemberOnLeave(LeaveRequest leave) {
        Map<String, Object> memberInfo = new HashMap<>();
        memberInfo.put("employeeName", leave.getEmployeeName());
        memberInfo.put("startDate", leave.getStartDate());
        memberInfo.put("endDate", leave.getEndDate());
        memberInfo.put("leaveType", leave.getLeaveType());
        memberInfo.put("reason", leave.getReason());
        return memberInfo;
    }

    private List<Map<String, Object>> mapLeaveRequests(List<LeaveRequest> requests) {
        return requests.stream()
                .map(this::mapLeaveRequestForHR)
                .collect(Collectors.toList());
    }

    private Map<String, Object> mapLeaveRequestForHR(LeaveRequest request) {
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
    }

    private Map<String, Object> mapNotification(LeaveRequest request) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("id", request.getId());
        notification.put("employeeName", request.getEmployeeName());
        notification.put("status", request.getStatus());
        notification.put("createdAt", request.getCreatedAt());
        notification.put("message", generateNotificationMessage(request));
        return notification;
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
}