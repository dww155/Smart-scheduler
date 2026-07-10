package com.dww.chat_app.controller;

import com.dww.chat_app.dto.ApiResponse;
import com.dww.chat_app.dto.auth.AuthRequest;
import com.dww.chat_app.dto.auth.AuthResponse;
import com.dww.chat_app.dto.auth.RegisterRequest;
import com.dww.chat_app.dto.user.UserCreationRequest;
import com.dww.chat_app.dto.user.UserResponse;
import com.dww.chat_app.entity.User;
import com.dww.chat_app.service.AuthService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AuthController {

    AuthService authService;

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);

        return ApiResponse.success("Login successfully", response);
    }

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);

        return ApiResponse.success("Register successfully", response);
    }

    @GetMapping("/myinfo")
    public ApiResponse<UserResponse> myInfo() {
        UserResponse response = authService.myInfo();
        return ApiResponse.success("Get user successfully", response);
    }
}
