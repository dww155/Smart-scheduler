package com.dww.chat_app.mapper;

import com.dww.chat_app.dto.workspace.WorkspaceCreationRequest;
import com.dww.chat_app.dto.workspace.WorkspaceResponse;
import com.dww.chat_app.dto.workspace.WorkspaceUpdateRequest;
import com.dww.chat_app.entity.User;
import com.dww.chat_app.entity.Workspace;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = TodoMapperConfig.class)
public interface WorkspaceMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "owner", source = "owner")
    @Mapping(target = "name", source = "request.name")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "type", source = "request.type")
    @Mapping(target = "color", source = "request.color")
    Workspace toEntity(WorkspaceCreationRequest request, User owner);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "name", source = "request.name")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "type", source = "request.type")
    @Mapping(target = "color", source = "request.color")
    void updateEntity(WorkspaceUpdateRequest request, @MappingTarget Workspace workspace);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "color", source = "color")
    @Mapping(target = "archivedAt", source = "archivedAt")
    @Mapping(target = "deletedAt", source = "deletedAt")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "version", source = "version")
    WorkspaceResponse toResponse(Workspace workspace);
}
