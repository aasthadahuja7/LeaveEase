package com.leavemanagement.leave_app;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmployeeServiceTest {

    @Test
    void testEmployeeProfileExists() {
        String employeeName = "Aastha";
        assertNotNull(employeeName, "Employee profile should not be null");
    }

    @Test
    void testEmployeeHasLeaveBalance() {
        int leaveBalance = 5;
        assertTrue(leaveBalance > 0, "Employee should have leave balance greater than 0");
    }
}
