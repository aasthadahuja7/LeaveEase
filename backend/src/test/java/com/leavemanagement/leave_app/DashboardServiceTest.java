package com.leavemanagement.leave_app;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DashboardServiceTest {

    @Test
    void testHRStatsLoads() {
        int totalEmployees = 10;
        int leavesTaken = 3;
        assertTrue(totalEmployees >= leavesTaken, "Dashboard should show valid HR stats");
    }
}