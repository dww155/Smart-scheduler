package com.dww.chat_app.util;

import com.dww.chat_app.entity.Role;
import com.dww.chat_app.entity.User;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtUtil {

    public static final String TOKEN_TYPE_CLAIM = "token_type";
    public static final String ACCESS_TOKEN_TYPE = "access";
    public static final String REFRESH_TOKEN_TYPE = "refresh";

    @NonFinal
    @Value("${jwt.secret}")
    String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.expiry-time}")
    long EXPIRY_TIME;

    @NonFinal
    @Value("${jwt.refreshable-time}")
    long REFRESHABLE_TIME;

    private String generateToken(User user, long expiryTime, String tokenType) {

        // create header
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);

        // create payload
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .jwtID(UUID.randomUUID().toString())
                .subject(user.getUsername())
                .claim("scope", buildScope(user))
                .claim(TOKEN_TYPE_CLAIM, tokenType)
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
            log.error("Unable to sign JWT", e);
            throw new IllegalStateException("Unable to sign JWT", e);
        }
        return object.serialize();
    }

    public String buildScope(User user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            return "";
        }

        return user.getRoles().stream()
                .map(Role::getName)
                .filter(roleName -> roleName != null && !roleName.isBlank())
                .map(roleName -> "ROLE_" + roleName.toUpperCase())
                .sorted()
                .collect(Collectors.joining(" "));
    }

    public String generateAcessToken(User user) {
        return generateAccessToken(user);
    }

    public String generateAccessToken(User user) {
        return generateToken(user, EXPIRY_TIME, ACCESS_TOKEN_TYPE);
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, REFRESHABLE_TIME, REFRESH_TOKEN_TYPE);
    }

    public SignedJWT verify(String token) {
        return verifyToken(token, null);
    }

    public SignedJWT verifyAccessToken(String token) {
        return verifyToken(token, ACCESS_TOKEN_TYPE);
    }

    public SignedJWT verifyRefreshToken(String token) {
        return verifyToken(token, REFRESH_TOKEN_TYPE);
    }

    private SignedJWT verifyToken(String token, String expectedTokenType) {
        try {
            // parse token
            SignedJWT jwt = SignedJWT.parse(token);

            // check valid signer
            MACVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
            boolean validSigner = jwt.verify(verifier);

            // check expiry time
            Date expirationTime = jwt.getJWTClaimsSet().getExpirationTime();
            Date now = new Date();
            boolean validExpiry = expirationTime != null && now.before(expirationTime);

            String tokenType = jwt.getJWTClaimsSet().getStringClaim(TOKEN_TYPE_CLAIM);
            boolean validType = expectedTokenType == null || expectedTokenType.equals(tokenType);

            // summary check
            if (!validSigner || !validExpiry || !validType)
                throw new BadJwtException("Invalid or expired token");

            return jwt;
        } catch (BadJwtException exception) {
            throw exception;
        } catch (ParseException | JOSEException | RuntimeException e) {
            throw new BadJwtException("Invalid token", e);
        }
    }
}
