package com.sha.employee_management.auth;

import com.sha.employee_management.entity.User;
import com.sha.employee_management.security.JwtService;
import com.sha.employee_management.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtService jwtService;
    private final UserService userService;

    public AuthController(JwtService jwtService,
                          UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public User register(@RequestBody User user) {

        return userService.register(user);
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request){

        User user = userService.findByUsername(request.getUsername());

        return jwtService.generateToken(user.getUsername());
    }
}
