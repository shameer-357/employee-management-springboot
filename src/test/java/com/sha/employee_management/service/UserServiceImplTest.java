package com.sha.employee_management.service;

import com.sha.employee_management.auth.AuthResponse;
import com.sha.employee_management.auth.RegisterRequest;
import com.sha.employee_management.dto.ChangePasswordRequest;
import com.sha.employee_management.dto.CreateUserRequest;
import com.sha.employee_management.dto.UserResponse;
import com.sha.employee_management.entity.Role;
import com.sha.employee_management.entity.User;
import com.sha.employee_management.exception.UserAlreadyExistsException;
import com.sha.employee_management.repository.UserRepository;
import com.sha.employee_management.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setUsername("testuser");
        sampleUser.setPassword("encodedOldPassword");
        sampleUser.setRole(Role.USER);
    }

    @Test
    @DisplayName("Register user successfully defaults to USER role")
    void testRegister_Success() {
        RegisterRequest request = new RegisterRequest("newuser", "plainPassword123");

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plainPassword123")).thenReturn("encodedPassword123");

        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setUsername("newuser");
        savedUser.setPassword("encodedPassword123");
        savedUser.setRole(Role.USER);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        AuthResponse response = userService.register(request);

        assertNotNull(response);
        assertEquals("newuser", response.getUsername());
        assertEquals("USER", response.getRole());
        assertEquals("User registered successfully", response.getMessage());

        verify(userRepository, times(1)).findByUsername("newuser");
        verify(passwordEncoder, times(1)).encode("plainPassword123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Register throws UserAlreadyExistsException when username already taken")
    void testRegister_DuplicateUsername_ThrowsConflict() {
        RegisterRequest request = new RegisterRequest("testuser", "password123");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(sampleUser));

        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () ->
                userService.register(request)
        );

        assertTrue(exception.getMessage().contains("already taken"));
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Change password succeeds when current password is valid")
    void testChangePassword_Success() {
        ChangePasswordRequest request = new ChangePasswordRequest("correctPassword", "newSecurePassword");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("correctPassword", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("newSecurePassword")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        assertDoesNotThrow(() -> userService.changePassword("testuser", request));

        assertEquals("encodedNewPassword", sampleUser.getPassword());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(passwordEncoder, times(1)).matches("correctPassword", "encodedOldPassword");
        verify(passwordEncoder, times(1)).encode("newSecurePassword");
        verify(userRepository, times(1)).save(sampleUser);
    }

    @Test
    @DisplayName("Change password throws IllegalArgumentException when current password is wrong")
    void testChangePassword_WrongCurrentPassword_ThrowsBadRequest() {
        ChangePasswordRequest request = new ChangePasswordRequest("wrongPassword", "newSecurePassword");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("wrongPassword", "encodedOldPassword")).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                userService.changePassword("testuser", request)
        );

        assertEquals("Current password is incorrect", exception.getMessage());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(passwordEncoder, times(1)).matches("wrongPassword", "encodedOldPassword");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Create user by Admin with specified Role succeeds")
    void testCreateUser_Admin_Success() {
        CreateUserRequest request = new CreateUserRequest("adminUser", "adminSecret", Role.ADMIN);

        when(userRepository.findByUsername("adminUser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("adminSecret")).thenReturn("encodedAdminSecret");

        User savedAdmin = new User();
        savedAdmin.setId(3L);
        savedAdmin.setUsername("adminUser");
        savedAdmin.setPassword("encodedAdminSecret");
        savedAdmin.setRole(Role.ADMIN);

        when(userRepository.save(any(User.class))).thenReturn(savedAdmin);

        UserResponse response = userService.createUser(request);

        assertNotNull(response);
        assertEquals(3L, response.getId());
        assertEquals("adminUser", response.getUsername());
        assertEquals(Role.ADMIN, response.getRole());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Create user by Admin throws UserAlreadyExistsException on duplicate")
    void testCreateUser_Duplicate_ThrowsConflict() {
        CreateUserRequest request = new CreateUserRequest("testuser", "secretPass", Role.ADMIN);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(sampleUser));

        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () ->
                userService.createUser(request)
        );

        assertTrue(exception.getMessage().contains("already exists"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Get all users returns list of UserResponse")
    void testGetAllUsers() {
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("admin");
        user2.setRole(Role.ADMIN);

        when(userRepository.findAll()).thenReturn(List.of(sampleUser, user2));

        List<UserResponse> list = userService.getAllUsers();

        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals("testuser", list.get(0).getUsername());
        assertEquals(Role.USER, list.get(0).getRole());
        assertEquals("admin", list.get(1).getUsername());
        assertEquals(Role.ADMIN, list.get(1).getRole());
    }

    @Test
    @DisplayName("Get user profile returns UserResponse")
    void testGetUserProfile() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(sampleUser));

        UserResponse response = userService.getUserProfile("testuser");

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("testuser", response.getUsername());
        assertEquals(Role.USER, response.getRole());
    }
}
