package com.dww.chat_app.dto.workspace;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class MemberCandidateResponse {

    UUID id;

    String username;

    String email;
}
