package com.dww.chat_app.mapper;

import com.dww.chat_app.dto.auth.RegisterRequest;
import com.dww.chat_app.dto.user.UserCreationRequest;
import com.dww.chat_app.dto.user.UserResponse;
import com.dww.chat_app.dto.user.UserUpdateRequest;
import com.dww.chat_app.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "roles", ignore = true)
    User toUser(UserCreationRequest request);

    User toUser(RegisterRequest request);

    UserResponse toUserResponse(User user);

    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
