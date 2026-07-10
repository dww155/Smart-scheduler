package com.dww.chat_app.service;

import com.dww.chat_app.dto.message.MessageSendRequest;
import com.dww.chat_app.dto.message.MessageSendResponse;
import com.dww.chat_app.entity.Message;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class MessageService {

//    public MessageSendResponse sendMessage(MessageSendRequest request) {
//        Message message = Message.builder()
//                .sender()
//                .build();
//
//        SecurityContextHolder.getContext().getAuthentication().;
//    }
}
