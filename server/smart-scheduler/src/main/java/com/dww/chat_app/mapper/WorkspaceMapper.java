package com.dww.chat_app.mapper;

import com.dww.chat_app.dto.workspace.WorkspaceCreationRequest;
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
}
