package com.leavemanagement.leave_app.service;

import com.leavemanagement.leave_app.model.LeaveRequest;
import com.leavemanagement.leave_app.repository.LeaveRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class LeaveRequestService {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    private static final int MAX_LEAVE_DAYS = 30;

    public LeaveRequest createLeaveRequest(LeaveRequest leaveRequest) {
        validateLeaveRequest(leaveRequest);
        return leaveRequestRepository.save(leaveRequest);
    }

    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveRequestRepository.findAll();
    }

    private void validateLeaveRequest(LeaveRequest leaveRequest) {
        if (leaveRequest.getStartDate() == null || leaveRequest.getEndDate() == null) {
            throw new IllegalArgumentException("Start date and end date are required");
        }

        if (leaveRequest.getEndDate().isBefore(leaveRequest.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        if (leaveRequest.getStartDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot apply for leave in the past");
        }

        long leaveDays = ChronoUnit.DAYS.between(leaveRequest.getStartDate(), leaveRequest.getEndDate()) + 1;
        if (leaveDays > MAX_LEAVE_DAYS) {
            throw new IllegalArgumentException("Leave request exceeds maximum allowed days (" + MAX_LEAVE_DAYS + " days)");
        }

        List<LeaveRequest> existingRequests = leaveRequestRepository.findByEmployeeNameAndStatus(
                leaveRequest.getEmployeeName(), "Approved");

        boolean hasOverlap = existingRequests.stream()
                .anyMatch(existing -> !leaveRequest.getEndDate().isBefore(existing.getStartDate())
                        && !leaveRequest.getStartDate().isAfter(existing.getEndDate()));

        if (hasOverlap) {
            throw new IllegalArgumentException("Leave request overlaps with an existing approved leave");
        }
    }
}
