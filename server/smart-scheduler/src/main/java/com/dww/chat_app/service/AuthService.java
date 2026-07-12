package com.dww.chat_app.service;

import com.dww.chat_app.constant.UserConstant;
import com.dww.chat_app.dto.auth.AuthRequest;
import com.dww.chat_app.dto.auth.AuthResponse;
import com.dww.chat_app.dto.auth.RegisterRequest;
import com.dww.chat_app.dto.user.UserResponse;
import com.dww.chat_app.entity.Role;
import com.dww.chat_app.entity.User;
import com.dww.chat_app.exception.AppException;
import com.dww.chat_app.exception.ErrorCode;
import com.dww.chat_app.mapper.UserMapper;
import com.dww.chat_app.repository.RoleRepository;
import com.dww.chat_app.repository.UserRepository;
import com.dww.chat_app.util.JwtUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AuthService {

    UserRepository userRepository;
    UserMapper userMapper;

    RoleRepository roleRepository;

    JwtUtil jwtUtil;

    PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {

        // find user with user name
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        // check user is validated or not
        if (user.getDeletedAt() != null || !user.isActive())
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        // check password
        boolean valid = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!valid)
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        // create tokens
        String accessToken = jwtUtil.generateAcessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        return AuthResponse.builder()
                .valid(true)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())
                || userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS);
        }

        User user = userMapper.toUser(request);

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role userRole = roleRepository.findById(UserConstant.ROLE_USER).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        user.setRoles(Set.of(userRole));

        User savedUser = userRepository.save(user);

        return AuthResponse.builder()
                .valid(true)
                .accessToken(jwtUtil.generateAcessToken(savedUser))
                .refreshToken(jwtUtil.generateRefreshToken(savedUser))
                .build();
    }

    @Transactional(readOnly = true)
    public UserResponse myInfo() {
        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        return userMapper.toUserResponse(user);
    }
}
