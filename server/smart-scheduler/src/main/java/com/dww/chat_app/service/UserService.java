package com.dww.chat_app.service;

import com.dww.chat_app.dto.user.UserCreationRequest;
import com.dww.chat_app.dto.user.UserResponse;
import com.dww.chat_app.dto.user.UserUpdateRequest;
import com.dww.chat_app.dto.user.UserUpdateStatusRequest;
import com.dww.chat_app.entity.Role;
import com.dww.chat_app.entity.User;
import com.dww.chat_app.exception.AppException;
import com.dww.chat_app.exception.ErrorCode;
import com.dww.chat_app.mapper.UserMapper;
import com.dww.chat_app.repository.RoleRepository;
import com.dww.chat_app.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UserService {

    UserRepository userRepository;
    UserMapper userMapper;

    RoleRepository roleRepository;

    PasswordEncoder passwordEncoder;

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse createUser(UserCreationRequest request) {
        validateUsernameIsAvailable(request.getUsername());
        validateEmailIsAvailable(request.getEmail());

        // mapping
        User user = userMapper.toUser(request);

        //set roles
        Set<Role> roles = new HashSet<>(roleRepository.findAllById(request.getRoleNames()));
        user.setRoles(roles);

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toUserResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUser(UUID userId) {
        return userMapper.toUserResponse(findUserById(userId));
    }

    @Transactional
    public UserResponse updateUser(UUID userId, UserUpdateRequest request) {
        User user = findUserById(userId);

        if (!user.getUsername().equals(request.getUsername())
                && userRepository.existsByUsernameAndIdNot(request.getUsername(), userId)) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS);
        }

        userMapper.updateUser(user, request);

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(UUID userId) {
        User user = findUserById(userId);

        user.setDeletedAt(LocalDateTime.now());
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void updateUserStatus(UUID userId, UserUpdateStatusRequest request) {
        User user = findUserById(userId);

        //set roles
        if (request.getRoleNames() != null) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(request.getRoleNames()));
            user.setRoles(roles);
        }

        if (request.getActive() != null)
            user.setActive(request.getActive().booleanValue());
    }

    private User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private void validateUsernameIsAvailable(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS);
        }
    }

    private void validateEmailIsAvailable(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS);
        }
    }
}
