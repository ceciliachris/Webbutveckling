package com.example.demo.security;

import com.example.demo.user.UserEntity;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;

/**
 * Filterklass för att hantera autentisering vid varje HTTP-anrop.
 */
@Service
@RequiredArgsConstructor
public class AuthFilter extends OncePerRequestFilter {

    private final AuthService authService;

    /**
     * Hanterar autentisering genom att verifiera JWT-token i förfrågan.
     * om en giltig token hittas sätts användaren i SecurityContext-
     *
     * @param request Http-förfrågan.
     * @param response Http-svar.
     * @param filterChain Filterkedjan för att skicka vidare förfrågan.
     * @throws ServletException Om ett servlet-relaterat fel uppstår.
     * @throws IOException Om ett IO-fel uppstår.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        System.out.println("Request URL: " + request.getRequestURI());

        if (request.getRequestURI().matches("^/register|/login|/oauth2/.*$") ||
                request.getRequestURI().startsWith("/oauth2/authorization")) {
            filterChain.doFilter(request, response);
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
            UserEntity user = authService.authenticateUser(token);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            logger.error("Authentication failed in filter", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid or expired token");
            response.getWriter().flush();
        }
    }
}
