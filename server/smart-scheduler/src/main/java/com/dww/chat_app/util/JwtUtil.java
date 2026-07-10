package com.dww.chat_app.util;

import com.dww.chat_app.entity.Role;
import com.dww.chat_app.entity.User;
import com.dww.chat_app.exception.AppException;
import com.dww.chat_app.exception.ErrorCode;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtUtil {

    @NonFinal
    @Value("${jwt.secret}")
    String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.expiry-time}")
    long EXPIRY_TIME;

    @NonFinal
    @Value("${jwt.refreshable-time}")
    long REFRESHABLE_TIME;

    private String generateToken(User user, long expiryTime) {

        // create header
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);

        // create payload
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .jwtID(UUID.randomUUID().toString())
                .subject(user.getUsername())
                .claim("scope", buildScope(user))
                .issuer("dww")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(expiryTime, ChronoUnit.SECONDS).toEpochMilli()))
                .build();
        Payload payload = new Payload(claimsSet.toJSONObject());

        // create jws object
        JWSObject object = new JWSObject(header, payload);

        // sign and return String token
        try {
            MACSigner signer = new MACSigner((SIGNER_KEY.getBytes()));
            object.sign(signer);
        } catch (JOSEException e) {
            log.info(e.getMessage());
            throw new RuntimeException(e);
        }
        return object.serialize();
    }

    private String buildScope(User user) {

        // build scope
        StringBuilder scopes = new StringBuilder();

        for (Role role : user.getRoles()) {
            String roleName = role.getName();

            scopes.append(String.format("ROLE_%s ", roleName.toUpperCase()));
        }

        return scopes.toString().trim();
    }

    public String generateAcessToken(User user) {
        return generateToken(user, EXPIRY_TIME);
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, REFRESHABLE_TIME);
    }

    public SignedJWT verify(String token) {
        try {
            // parse token
            SignedJWT jwt = SignedJWT.parse(token);

            // check valid signer
            MACVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
            boolean validSigner = jwt.verify(verifier);

            // check expiry time
            Date expirationTime = jwt.getJWTClaimsSet().getExpirationTime();
            Date now = new Date();
            boolean validExpiry = now.before(expirationTime);

            // summary check
            if (!validSigner || !validExpiry)
                throw new AppException(ErrorCode.UNAUTHORIZED);

            return jwt;
        } catch (ParseException | JOSEException e) {
            throw new RuntimeException(e);
        }
    }
}
