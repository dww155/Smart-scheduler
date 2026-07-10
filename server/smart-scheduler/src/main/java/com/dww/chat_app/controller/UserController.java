package com.dww.chat_app.controller;

import com.dww.chat_app.dto.ApiResponse;
import com.dww.chat_app.dto.user.UserCreationRequest;
import com.dww.chat_app.dto.user.UserResponse;
import com.dww.chat_app.dto.user.UserUpdateRequest;
import com.dww.chat_app.dto.user.UserUpdateStatusRequest;
import com.dww.chat_app.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/user")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UserController {
    UserService userService;

    @PostMapping
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody UserCreationRequest request) {
        return ApiResponse.success("Create user successfully", userService.createUser(request));
    }

    @GetMapping
    public ApiResponse<List<UserResponse>> getUsers() {
        return ApiResponse.success(userService.getUsers());
    }

    @GetMapping("/userId={userId}")
    public ApiResponse<UserResponse> getUser(@PathVariable("userId") UUID userId) {
        return ApiResponse.success(userService.getUser(userId));
    }

    @PutMapping("/userId={userId}")
    public ApiResponse<UserResponse> updateUser(
            @PathVariable("userId") UUID userId,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        return ApiResponse.success("Update user successfully", userService.updateUser(userId, request));
    }

    @PatchMapping("/userId={userId}/status")
    public ApiResponse<Void> updateUserStatus(
            @PathVariable("userId") UUID userId,
            @Valid @RequestBody UserUpdateStatusRequest request
    ) {
        userService.updateUserStatus(userId, request);

        return ApiResponse.success("Update user status successfully");
    }

    @DeleteMapping("/userId={userId}")
    public ApiResponse<Void> deleteUser(@PathVariable("userId") UUID userId) {
        userService.deleteUser(userId);

        return ApiResponse.success("Delete user successfully");
    }
}
