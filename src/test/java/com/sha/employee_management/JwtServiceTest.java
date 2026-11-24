package com.sha.employee_management;

import com.sha.employee_management.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private final String testSecret = "1CodeWithPurpose2BuildWithPassion3NeverStopLearning4";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", testSecret);
    }

    @Test
    void testGenerateAndExtractClaims() {
        String token = jwtService.generateToken("john_doe", "ADMIN");
        assertNotNull(token);

        String username = jwtService.extractUsername(token);
        assertEquals("john_doe", username);

        String role = jwtService.extractRole(token);
        assertEquals("ADMIN", role);

        assertTrue(jwtService.isTokenValid(token, "john_doe"));
        assertFalse(jwtService.isTokenValid(token, "different_user"));
    }

    @Test
    void testDefaultRoleInGenerateToken() {
        String token = jwtService.generateToken("user123");
        assertNotNull(token);

        String role = jwtService.extractRole(token);
        assertEquals("USER", role);
    }
}
