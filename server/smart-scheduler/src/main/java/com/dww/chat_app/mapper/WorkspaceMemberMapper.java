package com.dww.chat_app.mapper;

import com.dww.chat_app.dto.workspace.WorkspaceMemberCreationRequest;
import com.dww.chat_app.dto.workspace.WorkspaceMemberRoleUpdateRequest;
import com.dww.chat_app.dto.workspace.WorkspaceMemberResponse;
import com.dww.chat_app.entity.User;
import com.dww.chat_app.entity.Workspace;
import com.dww.chat_app.entity.WorkspaceMember;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = TodoMapperConfig.class)
public interface WorkspaceMemberMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "workspace", source = "workspace")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "role", source = "request.role")
    WorkspaceMember toEntity(
            WorkspaceMemberCreationRequest request,
            Workspace workspace,
            User user
    );

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "role", source = "request.role")
    void updateRole(
            WorkspaceMemberRoleUpdateRequest request,
            @MappingTarget WorkspaceMember workspaceMember
    );

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "workspaceId", source = "workspace.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "joinedAt", source = "joinedAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "version", source = "version")
    WorkspaceMemberResponse toResponse(WorkspaceMember workspaceMember);
}
