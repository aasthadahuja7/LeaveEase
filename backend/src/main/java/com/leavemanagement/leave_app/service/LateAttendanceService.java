package com.leavemanagement.leave_app.service;

import com.leavemanagement.leave_app.model.LateAttendance;
import com.leavemanagement.leave_app.repository.LateAttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class LateAttendanceService {

    @Autowired
    private LateAttendanceRepository lateAttendanceRepository;

    @Autowired
    private EmployeeService employeeService;

    public LateAttendance markEmployeeLate(String employeeName, LocalDate date,
                                           String reason, String markedBy, String notes) {

        if (lateAttendanceRepository.existsByEmployeeNameAndDate(employeeName, date)) {
            throw new RuntimeException("Employee already marked as late on " + date);
        }

        String employeeId = employeeService.getEmployeeIdByName(employeeName);

        LateAttendance lateAttendance = new LateAttendance(
                employeeName,
                employeeId,
                date,
                reason,
                markedBy
        );

        lateAttendance.setNotes(notes);

        return lateAttendanceRepository.save(lateAttendance);
    }

    public java.util.List<LateAttendance> getLateAttendanceForEmployee(String employeeName) {
        return lateAttendanceRepository.findByEmployeeNameOrderByDateDesc(employeeName);
    }

    public java.util.List<LateAttendance> getLateAttendanceForEmployeeInRange(
            String employeeName,
            LocalDate startDate,
            LocalDate endDate) {

        return lateAttendanceRepository.findByEmployeeNameAndDateBetween(
                employeeName,
                startDate,
                endDate
        );
    }

    public java.util.List<LateAttendance> getLateAttendanceForDate(LocalDate date) {
        return lateAttendanceRepository.findByDate(date);
    }

    public java.util.List<LateAttendance> getLateAttendanceInRange(
            LocalDate startDate,
            LocalDate endDate) {

        return lateAttendanceRepository.findByDateBetween(startDate, endDate);
    }

    public boolean isEmployeeLateOnDate(String employeeName, LocalDate date) {
        return lateAttendanceRepository.existsByEmployeeNameAndDate(employeeName, date);
    }

    public Optional<LateAttendance> getLateAttendanceById(String id) {
        return lateAttendanceRepository.findById(id);
    }

    public LateAttendance updateLateAttendance(String id, String reason, String notes) {

        LateAttendance lateAttendance = lateAttendanceRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Late attendance record not found with ID: " + id));

        lateAttendance.setReason(reason);
        lateAttendance.setNotes(notes);

        return lateAttendanceRepository.save(lateAttendance);
    }

    public void deleteLateAttendance(String id) {
        lateAttendanceRepository.deleteById(id);
    }

    public long getLateDaysCountForEmployeeInMonth(String employeeName, int year, int month) {

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        return lateAttendanceRepository
                .findByEmployeeNameAndDateBetween(employeeName, startDate, endDate)
                .size();
    }
}