package com.dww.chat_app.mapper;

import com.dww.chat_app.dto.project.ProjectCreationRequest;
import com.dww.chat_app.dto.project.ProjectResponse;
import com.dww.chat_app.dto.project.ProjectUpdateRequest;
import com.dww.chat_app.entity.Project;
import com.dww.chat_app.entity.User;
import com.dww.chat_app.entity.Workspace;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = TodoMapperConfig.class)
public interface ProjectMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "workspace", source = "workspace")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "name", source = "request.name")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "color", source = "request.color")
    @Mapping(target = "icon", source = "request.icon")
    @Mapping(target = "viewType", source = "request.viewType")
    @Mapping(target = "sortOrder", source = "request.sortOrder")
    Project toEntity(ProjectCreationRequest request, Workspace workspace, User createdBy);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "name", source = "request.name")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "color", source = "request.color")
    @Mapping(target = "icon", source = "request.icon")
    @Mapping(target = "viewType", source = "request.viewType")
    @Mapping(target = "sortOrder", source = "request.sortOrder")
    void updateEntity(ProjectUpdateRequest request, @MappingTarget Project project);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "workspaceId", source = "workspace.id")
    @Mapping(target = "createdById", source = "createdBy.id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "color", source = "color")
    @Mapping(target = "icon", source = "icon")
    @Mapping(target = "viewType", source = "viewType")
    @Mapping(target = "sortOrder", source = "sortOrder")
    @Mapping(target = "archivedAt", source = "archivedAt")
    @Mapping(target = "deletedAt", source = "deletedAt")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "version", source = "version")
    ProjectResponse toResponse(Project project);
}
