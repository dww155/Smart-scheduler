package com.dww.chat_app.mapper;

import com.dww.chat_app.dto.project.TaskSectionCreationRequest;
import com.dww.chat_app.dto.project.TaskSectionUpdateRequest;
import com.dww.chat_app.entity.Project;
import com.dww.chat_app.entity.TaskSection;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = TodoMapperConfig.class)
public interface TaskSectionMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "project", source = "project")
    @Mapping(target = "name", source = "request.name")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "sortOrder", source = "request.sortOrder")
    TaskSection toEntity(TaskSectionCreationRequest request, Project project);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "name", source = "request.name")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "sortOrder", source = "request.sortOrder")
    void updateEntity(TaskSectionUpdateRequest request, @MappingTarget TaskSection section);
}
