package com.leavemanagement.leave_app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.leavemanagement.leave_app.model.LeaveRequest;
import com.leavemanagement.leave_app.repository.LeaveRequestRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private static final int MAX_LEAVE_DAYS = 30; // Maximum leave days allowed

    @Autowired
    public LeaveRequestService(LeaveRequestRepository leaveRequestRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
    }
// validations in backend 
    public LeaveRequest createLeaveRequest(LeaveRequest leaveRequest) {
        validateLeaveRequest(leaveRequest);
        return leaveRequestRepository.save(leaveRequest);
    }

    private void validateLeaveRequest(LeaveRequest leaveRequest) {
        // Check if dates are provided
        if (leaveRequest.getStartDate() == null || leaveRequest.getEndDate() == null) {
            throw new IllegalArgumentException("Start date and end date are required");
        }

        // Check if end date is after start date
        if (leaveRequest.getEndDate().isBefore(leaveRequest.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        // Check if start date is not in the past
        if (leaveRequest.getStartDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot apply for leave in the past");
        }

        // Calculate leave duration
        long leaveDays = ChronoUnit.DAYS.between(leaveRequest.getStartDate(), leaveRequest.getEndDate()) + 1;
        
        // Check maximum leave duration
        if (leaveDays > MAX_LEAVE_DAYS) {
            throw new IllegalArgumentException("Leave request exceeds maximum allowed days (" + MAX_LEAVE_DAYS + " days)");
        }

        // Check for overlapping leave requests
        List<LeaveRequest> existingRequests = leaveRequestRepository.findByEmployeeNameAndStatus(
            leaveRequest.getEmployeeName(), "Approved");
        
        for (LeaveRequest existing : existingRequests) {
            if (!(leaveRequest.getEndDate().isBefore(existing.getStartDate()) || 
                  leaveRequest.getStartDate().isAfter(existing.getEndDate()))) {
                throw new IllegalArgumentException("Leave request overlaps with an existing approved leave");
            }
        }
    }

    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveRequestRepository.findAll();
    }
}
