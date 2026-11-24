package com.sha.employee_management.service;

import com.sha.employee_management.auth.AuthResponse;
import com.sha.employee_management.auth.RegisterRequest;
import com.sha.employee_management.dto.ChangePasswordRequest;
import com.sha.employee_management.dto.CreateUserRequest;
import com.sha.employee_management.dto.UserResponse;
import com.sha.employee_management.entity.User;

import java.util.List;

public interface UserService {

    AuthResponse register(RegisterRequest request);

    User register(User user);

    User findByUsername(String username);

    UserResponse getUserProfile(String username);

    void changePassword(String username, ChangePasswordRequest request);

    List<UserResponse> getAllUsers();

    UserResponse createUser(CreateUserRequest request);
}
