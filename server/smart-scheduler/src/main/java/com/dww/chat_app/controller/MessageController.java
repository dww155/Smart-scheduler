package com.dww.chat_app.controller;

import com.dww.chat_app.dto.message.MessageSendRequest;
import jakarta.validation.Valid;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class MessageController {

    @MessageMapping("/messages/send")
    @SendTo("/topic/messages")
    public MessageSendRequest sendMessage(@Valid @Payload MessageSendRequest request) {
        return request;
    }
}
