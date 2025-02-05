package com.example.demo.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.demo.user.UserEntity;
import com.example.demo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Service-klass för att hantera autentisering och JWT-verifiering.
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final Algorithm algorithm = Algorithm.HMAC256("secretsecretsecret");

    /**
     * Verifierar JWT-token och autentiserar användaren.
     * @param token JWT-token från klienten.
     * @throws Exception Om token är ogiltig eller om användaren inte hittas.
     */
    public UserEntity authenticateUser(String token) throws Exception {
        JWTVerifier verifier = JWT.require(algorithm).withIssuer("auth0").build();
        DecodedJWT jwt = verifier.verify(token);

        UUID userId = UUID.fromString(jwt.getSubject());
        return userRepository.findById(userId).orElseThrow();
    }
}