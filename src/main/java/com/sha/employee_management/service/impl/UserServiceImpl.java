package com.sha.employee_management.service.impl;

import com.sha.employee_management.auth.AuthResponse;
import com.sha.employee_management.auth.RegisterRequest;
import com.sha.employee_management.dto.ChangePasswordRequest;
import com.sha.employee_management.dto.CreateUserRequest;
import com.sha.employee_management.dto.UserResponse;
import com.sha.employee_management.entity.Role;
import com.sha.employee_management.entity.User;
import com.sha.employee_management.exception.UserAlreadyExistsException;
import com.sha.employee_management.repository.UserRepository;
import com.sha.employee_management.service.UserService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository repository,
                           PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (repository.findByUsername(request.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Username '" + request.getUsername() + "' is already taken");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER); // Default new registrations safely to USER role

        User savedUser = repository.save(user);

        return new AuthResponse(savedUser.getUsername(), savedUser.getRole().name(), "User registered successfully");
    }

    @Override
    public User register(User user) {
        if (repository.findByUsername(user.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Username '" + user.getUsername() + "' is already taken");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER); // Default new registrations safely to USER role
        return repository.save(user);
    }

    @Override
    public User findByUsername(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    @Override
    public UserResponse getUserProfile(String username) {
        User user = findByUsername(username);
        return new UserResponse(user.getId(), user.getUsername(), user.getRole());
    }

    @Override
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = findByUsername(username);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        repository.save(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return repository.findAll().stream()
                .map(user -> new UserResponse(user.getId(), user.getUsername(), user.getRole()))
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        if (repository.findByUsername(request.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Username '" + request.getUsername() + "' already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        User savedUser = repository.save(user);
        return new UserResponse(savedUser.getId(), savedUser.getUsername(), savedUser.getRole());
    }
}
