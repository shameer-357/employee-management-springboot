package com.sha.employee_management.service.impl;

import com.sha.employee_management.entity.User;
import com.sha.employee_management.repository.UserRepository;
import com.sha.employee_management.service.UserService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    public User register(User user) {

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return repository.save(user);
    }

    @Override
    public User findByUsername(String username) {

        return repository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}