package com.example.demo.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.demo.user.UserEntity;
import com.example.demo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final AuthFilter authFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final UserRepository userRepository;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/register", "/login", "/oauth2/**","/error").permitAll()
                        .requestMatchers("/api/folder/**").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler((request, response, authentication) -> {
                            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                            String token = generateJwtToken(oauth2User);

                            response.setContentType("application/json");
                            response.getWriter().write("{\"token\": \"" + token + "\"}");
                            response.getWriter().flush();
                            //response.sendRedirect("http://localhost:3000/oauth2/callback?token=" + token);
                        })
                )
                .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private String generateJwtToken(OAuth2User oauth2User) {
        Algorithm algorithm = Algorithm.HMAC256("secretsecretsecret");

        String githubId = oauth2User.getAttribute("id").toString();
        UserEntity user = userRepository.findByProviderAndProviderId("github", githubId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return JWT.create()
                .withIssuer("auth0")
                .withSubject(user.getId().toString())
                .withClaim("login", oauth2User.getAttribute("login").toString())
                .withExpiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .sign(algorithm);
    }
}
