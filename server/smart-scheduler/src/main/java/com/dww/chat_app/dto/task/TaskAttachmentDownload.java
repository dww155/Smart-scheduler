package com.dww.chat_app.dto.task;

import lombok.Builder;
import lombok.Value;
import org.springframework.core.io.Resource;

@Value
@Builder
public class TaskAttachmentDownload {

    Resource resource;

    String originalFileName;

    String contentType;

    long sizeBytes;
}
