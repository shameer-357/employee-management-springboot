package com.sha.employee_management;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sha.employee_management.auth.AuthController;
import com.sha.employee_management.auth.AuthResponse;
import com.sha.employee_management.auth.LoginRequest;
import com.sha.employee_management.auth.RegisterRequest;
import com.sha.employee_management.entity.Role;
import com.sha.employee_management.entity.User;
import com.sha.employee_management.exception.GlobalExceptionHandler;
import com.sha.employee_management.security.JwtService;
import com.sha.employee_management.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthController authController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testRegisterSuccess_ReturnsAuthResponseWithoutPassword() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("newUser", "password123");
        AuthResponse authResponse = new AuthResponse("newUser", "USER", "User registered successfully");

        when(userService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newUser"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void testLoginSuccess_ValidPassword() throws Exception {
        LoginRequest loginRequest = new LoginRequest("testUser", "validPassword");
        User user = new User();
        user.setUsername("testUser");
        user.setPassword("encodedPassword");
        user.setRole(Role.USER);

        when(userService.findByUsername("testUser")).thenReturn(user);
        when(passwordEncoder.matches("validPassword", "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken("testUser", "USER")).thenReturn("mock-jwt-token");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void testLoginFailure_InvalidPassword_Returns401() throws Exception {
        LoginRequest loginRequest = new LoginRequest("testUser", "wrongPassword");
        User user = new User();
        user.setUsername("testUser");
        user.setPassword("encodedPassword");
        user.setRole(Role.USER);

        when(userService.findByUsername("testUser")).thenReturn(user);
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void testLoginFailure_UserNotFound_Returns401() throws Exception {
        LoginRequest loginRequest = new LoginRequest("unknownUser", "anyPassword");

        when(userService.findByUsername("unknownUser")).thenThrow(new BadCredentialsException("Invalid username or password"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }
}
