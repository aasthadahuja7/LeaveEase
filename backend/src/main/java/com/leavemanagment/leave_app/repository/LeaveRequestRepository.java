package com.leavemanagment.leave_app.repository;

import com.leavemanagment.leave_app.model.LeaveRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends MongoRepository<LeaveRequest, String> {
    
    // Find by employee name
    List<LeaveRequest> findByEmployeeName(String employeeName);
    
    // Find by status
    List<LeaveRequest> findByStatus(String status);
    
    // Find by date range
    List<LeaveRequest> findByStartDateBetween(LocalDate startDate, LocalDate endDate);
    
    // Find approved leaves for current date range (for team members on leave)
    @Query("{'status': 'Approved', 'startDate': {$lte: ?0}, 'endDate': {$gte: ?0}}")
    List<LeaveRequest> findCurrentlyOnLeave(LocalDate currentDate);
    
    // Find upcoming approved leaves
    @Query("{'status': 'Approved', 'startDate': {$gt: ?0}}")
    List<LeaveRequest> findUpcomingLeaves(LocalDate currentDate);
    
    // Count by status
    long countByStatus(String status);
    
    // Find recent requests (for notifications)
    List<LeaveRequest> findTop5ByOrderByIdDesc();
}

