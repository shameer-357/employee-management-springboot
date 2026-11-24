package com.sha.employee_management;

import com.sha.employee_management.config.DataInitializer;
import com.sha.employee_management.entity.Role;
import com.sha.employee_management.entity.User;
import com.sha.employee_management.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DataInitializer dataInitializer;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(dataInitializer, "adminUsername", "admin");
        ReflectionTestUtils.setField(dataInitializer, "adminPassword", "Admin@123");
    }

    @Test
    void testInitAdmin_WhenAdminDoesNotExist() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Admin@123")).thenReturn("encodedAdminPassword");

        dataInitializer.run();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(captor.capture());

        User saved = captor.getValue();
        assertEquals("admin", saved.getUsername());
        assertEquals("encodedAdminPassword", saved.getPassword());
        assertEquals(Role.ADMIN, saved.getRole());
    }

    @Test
    void testInitAdmin_WhenAdminAlreadyExists_Skips() {
        User existingAdmin = new User();
        existingAdmin.setUsername("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(existingAdmin));

        dataInitializer.run();

        verify(userRepository, never()).save(any(User.class));
    }
}
