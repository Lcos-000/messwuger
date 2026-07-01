package com.campusassistant.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import com.campusassistant.properties.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;


    public String genToken(Map<String, Object> claims) {
        return JWT.create()
                .withClaim(jwtProperties.getJwtTokenName(), claims)
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtProperties.getJwtTtl()))
                .sign(Algorithm.HMAC256(jwtProperties.getJwtSecretKey()));
    }

    public Map<String, Object> parseToken(String token) {
        return JWT.require(Algorithm.HMAC256(jwtProperties.getJwtSecretKey()))
                .build()
                .verify(token)
                .getClaim(jwtProperties.getJwtTokenName())
                .asMap();
    }
}
