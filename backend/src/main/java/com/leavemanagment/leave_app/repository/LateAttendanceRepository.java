package com.leavemanagment.leave_app.repository;

import com.leavemanagment.leave_app.model.LateAttendance;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LateAttendanceRepository extends MongoRepository<LateAttendance, String> {
    
    // Find all late attendance records for a specific employee
    List<LateAttendance> findByEmployeeNameOrderByDateDesc(String employeeName);
    
    // Find late attendance records for a specific employee within a date range
    @Query("{'employeeName': ?0, 'date': {$gte: ?1, $lte: ?2}}")
    List<LateAttendance> findByEmployeeNameAndDateBetween(String employeeName, LocalDate startDate, LocalDate endDate);
    
    // Find all late attendance records for a specific date
    List<LateAttendance> findByDate(LocalDate date);
    
    // Find all late attendance records within a date range
    @Query("{'date': {$gte: ?0, $lte: ?1}}")
    List<LateAttendance> findByDateBetween(LocalDate startDate, LocalDate endDate);
    
    // Check if an employee was late on a specific date
    boolean existsByEmployeeNameAndDate(String employeeName, LocalDate date);
} 