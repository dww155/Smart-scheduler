package com.dww.chat_app.mapper;

import com.dww.chat_app.dto.label.LabelCreationRequest;
import com.dww.chat_app.dto.label.LabelUpdateRequest;
import com.dww.chat_app.entity.Label;
import com.dww.chat_app.entity.User;
import com.dww.chat_app.entity.Workspace;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = TodoMapperConfig.class)
public interface LabelMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "workspace", source = "workspace")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "name", source = "request.name")
    @Mapping(target = "color", source = "request.color")
    Label toEntity(LabelCreationRequest request, Workspace workspace, User createdBy);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "name", source = "request.name")
    @Mapping(target = "color", source = "request.color")
    void updateEntity(LabelUpdateRequest request, @MappingTarget Label label);
}
