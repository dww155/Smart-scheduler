package com.dww.chat_app.util;

import com.dww.chat_app.entity.Role;
import com.dww.chat_app.entity.User;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.BadJwtException;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTests {

    JwtUtil jwtUtil;
    User user;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        jwtUtil.SIGNER_KEY = "aWNld2lzaHJhdGhlcm5vYm9keWRyYXdwcm92aWRlaHVuZHJlZHBhc3R0cnlzZW50cHU=";
        jwtUtil.EXPIRY_TIME = 3600;
        jwtUtil.REFRESHABLE_TIME = 604800;

        user = User.builder()
                .username("admin")
                .password("encoded")
                .roles(new HashSet<>(Set.of(
                        Role.builder().name("user").build(),
                        Role.builder().name("admin").build()
                )))
                .build();
    }

    @Test
    void accessAndRefreshTokensHaveDistinctTypes() throws Exception {
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        SignedJWT verifiedAccess = jwtUtil.verifyAccessToken(accessToken);
        SignedJWT verifiedRefresh = jwtUtil.verifyRefreshToken(refreshToken);

        assertThat(verifiedAccess.getJWTClaimsSet().getStringClaim(JwtUtil.TOKEN_TYPE_CLAIM))
                .isEqualTo(JwtUtil.ACCESS_TOKEN_TYPE);
        assertThat(verifiedRefresh.getJWTClaimsSet().getStringClaim(JwtUtil.TOKEN_TYPE_CLAIM))
                .isEqualTo(JwtUtil.REFRESH_TOKEN_TYPE);
        assertThatThrownBy(() -> jwtUtil.verifyAccessToken(refreshToken))
                .isInstanceOf(BadJwtException.class);
    }

    @Test
    void scopeIsStableAndSorted() {
        assertThat(jwtUtil.buildScope(user)).isEqualTo("ROLE_ADMIN ROLE_USER");
    }
}
