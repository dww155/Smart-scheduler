package com.dww.chat_app.configuration;

import com.dww.chat_app.entity.User;
import com.dww.chat_app.repository.InvalidatedTokenRepository;
import com.dww.chat_app.repository.UserRepository;
import com.dww.chat_app.util.JwtUtil;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.text.ParseException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomJwtDecoder implements JwtDecoder {

    JwtUtil jwtUtil;

    UserRepository userRepository;

    InvalidatedTokenRepository invalidatedTokenRepository;

    @NonFinal
    NimbusJwtDecoder nimbusJwtDecoder = null;

    @NonFinal
    @Value("${jwt.secret}")
    String SIGNER_KEY;

    @Override
    public Jwt decode(String token) throws JwtException {
        SignedJWT jwt = jwtUtil.verifyAccessToken(token);

        String username;

        try {
            username = jwt.getJWTClaimsSet().getSubject();
            UUID jwtId = UUID.fromString(jwt.getJWTClaimsSet().getJWTID());
            if (invalidatedTokenRepository.existsById(jwtId)) {
                throw new BadJwtException("Token has been revoked");
            }
        } catch (ParseException | IllegalArgumentException e) {
            throw new BadJwtException("Invalid token claims", e);
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadJwtException("User no longer exists"));

        if (user.getDeletedAt() != null || !user.isActive())
            throw new BadJwtException("User is not active");

        try {
            String tokenScope = jwt.getJWTClaimsSet().getStringClaim("scope");
            if (!jwtUtil.buildScope(user).equals(tokenScope)) {
                throw new BadJwtException("User permissions have changed");
            }
        } catch (ParseException e) {
            throw new BadJwtException("Invalid token scope", e);
        }

        SecretKeySpec spec = new SecretKeySpec(SIGNER_KEY.getBytes(), "H256");
        if (nimbusJwtDecoder == null)
            nimbusJwtDecoder = NimbusJwtDecoder
                    .withSecretKey(spec)
                    .macAlgorithm(MacAlgorithm.HS256)
                    .build();

        return nimbusJwtDecoder.decode(token);
    }
}
