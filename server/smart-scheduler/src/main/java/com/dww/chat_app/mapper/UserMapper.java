package com.dww.chat_app.mapper;

import com.dww.chat_app.dto.auth.RegisterRequest;
import com.dww.chat_app.dto.user.UserCreationRequest;
import com.dww.chat_app.dto.user.UserResponse;
import com.dww.chat_app.dto.user.UserUpdateRequest;
import com.dww.chat_app.entity.User;
import com.dww.chat_app.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "roles", ignore = true)
    User toUser(UserCreationRequest request);

    User toUser(RegisterRequest request);

    @Mapping(target = "roleNames", expression = "java(toRoleNames(user.getRoles()))")
    UserResponse toUserResponse(User user);

    void updateUser(@MappingTarget User user, UserUpdateRequest request);

    default Set<String> toRoleNames(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return Collections.emptySet();
        }

        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toUnmodifiableSet());
    }
}
