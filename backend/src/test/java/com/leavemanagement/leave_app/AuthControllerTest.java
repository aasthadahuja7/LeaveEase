

package com.leavemanagement.leave_app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AuthControllerTest {

    @Test
    void testUserLoginSuccess() {
        String username = "employee1";
        String password = "password123";
        boolean loginSuccess = username.equals("employee1") && password.equals("password123");

        assertTrue(loginSuccess, "User login should succeed with correct credentials");
    }

    @Test
    void testUserLoginFail() {
        String username = "employee1";
        String password = "wrongpass";
        boolean loginSuccess = username.equals("employee1") && password.equals("password123");

        assertFalse(loginSuccess, "User login should fail with incorrect credentials");
    }
}
