package com.sha.employee_management;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sha.employee_management.controller.UserController;
import com.sha.employee_management.dto.ChangePasswordRequest;
import com.sha.employee_management.dto.CreateUserRequest;
import com.sha.employee_management.dto.UserResponse;
import com.sha.employee_management.entity.Role;
import com.sha.employee_management.exception.GlobalExceptionHandler;
import com.sha.employee_management.exception.UserAlreadyExistsException;
import com.sha.employee_management.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testGetCurrentUser_ReturnsProfile() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("testuser", null, List.of())
        );

        when(userService.getUserProfile("testuser"))
                .thenReturn(new UserResponse(1L, "testuser", Role.USER));

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void testChangePassword_Success() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("testuser", null, List.of())
        );

        ChangePasswordRequest request = new ChangePasswordRequest("oldSecret", "newSecret123");
        doNothing().when(userService).changePassword(eq("testuser"), any(ChangePasswordRequest.class));

        mockMvc.perform(put("/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully"));
    }

    @Test
    void testChangePassword_IncorrectCurrentPassword_Returns400() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("testuser", null, List.of())
        );

        ChangePasswordRequest request = new ChangePasswordRequest("wrongOldSecret", "newSecret123");
        doThrow(new IllegalArgumentException("Current password is incorrect"))
                .when(userService).changePassword(eq("testuser"), any(ChangePasswordRequest.class));

        mockMvc.perform(put("/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Current password is incorrect"));
    }

    @Test
    void testGetAllUsers_ReturnsList() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(
                new UserResponse(1L, "admin", Role.ADMIN),
                new UserResponse(2L, "user1", Role.USER)
        ));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("admin"))
                .andExpect(jsonPath("$[1].username").value("user1"));
    }

    @Test
    void testCreateUser_Success() throws Exception {
        CreateUserRequest request = new CreateUserRequest("newAdmin", "secretPassword", Role.ADMIN);
        UserResponse response = new UserResponse(10L, "newAdmin", Role.ADMIN);

        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.username").value("newAdmin"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void testCreateUser_DuplicateUsername_Returns409() throws Exception {
        CreateUserRequest request = new CreateUserRequest("existingUser", "secretPassword", Role.USER);

        when(userService.createUser(any(CreateUserRequest.class)))
                .thenThrow(new UserAlreadyExistsException("Username 'existingUser' already exists"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Username 'existingUser' already exists"));
    }
}
