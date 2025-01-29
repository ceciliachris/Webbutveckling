package com.example.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * Filterklass för att hantera autentisering vid varje HTTP-anrop.
 */
@Service
@RequiredArgsConstructor
public class AuthFilter extends OncePerRequestFilter {

    private final AuthService authService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        System.out.println("Request URL: " + request.getRequestURI());

        if (request.getRequestURI().equals("/register") || request.getRequestURI().equals("/login")) {
            filterChain.doFilter(request,response);
            return;
        }

       String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.isBlank() || !authHeader.startsWith("Bearer ")) {
            System.out.println("Invalid auth header format");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Authorization header is missing or malformed");
            response.getWriter().flush();
            return;
        }

        String token = authHeader.substring(7);
        try {
            authService.authenticateUser(token);
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
