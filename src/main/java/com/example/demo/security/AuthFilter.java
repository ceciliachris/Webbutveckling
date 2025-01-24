package com.example.demo.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.demo.models.User;
import com.example.demo.repositories.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class AuthFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    @Autowired
    public AuthFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        System.out.println("Request URL: " + request.getRequestURI());
        String authHeader = request.getHeader("Authorization");
        System.out.println("Auth header received: " + authHeader);

        if (request.getRequestURI().equals("/register") || request.getRequestURI().equals("/login")) {
            filterChain.doFilter(request,response);
            return;
        }

        authHeader = request.getHeader("Authorization");
        System.out.println("Auth header received: " + authHeader);

        if (authHeader == null || authHeader.isBlank() || !authHeader.startsWith("Bearer ")) {
            System.out.println("Invalid auth header format");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Authorization header is missing or malformed");
            response.getWriter().flush();
            return;
        }

        String token = authHeader.substring(7);
        System.out.println("Token extracted: " + token);

        try {
        Algorithm algorithm = Algorithm.HMAC256("secretsecretsecret");
        JWTVerifier verifier = JWT.require(algorithm).withIssuer("auth0").build();

        DecodedJWT jwt = verifier.verify(token);
            System.out.println("JWT verified successfully");
        UUID userId = UUID.fromString(jwt.getSubject());
            System.out.println("User ID from token: " + userId);

        User user = userRepository.findById(userId).orElseThrow();
            System.out.println("User found: " + user.getName());

            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>())
            );

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            System.out.println("Error in filter: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid or expired token");
            response.getWriter().flush();
        }
    }
}
