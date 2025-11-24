package com.FindMyService.controller;

import com.FindMyService.model.dto.LoginRequestDto;
import com.FindMyService.model.dto.RegisterRequestDto;
import com.FindMyService.service.AuthService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public Map<String, String> register(@RequestBody RegisterRequestDto request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequestDto request) {
        return authService.login(request);
    }
}
