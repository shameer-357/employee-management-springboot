package com.sha.employee_management.service;

import com.sha.employee_management.entity.User;

public interface UserService {

    User register(User user);

    User findByUsername(String username);

}