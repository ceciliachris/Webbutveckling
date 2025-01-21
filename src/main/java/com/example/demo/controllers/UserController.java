package com.example.demo.controllers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.demo.models.User;
import com.example.demo.repositories.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/test")
    public String test() {
        return "Du är inloggad!";
    }

    @GetMapping("/current-user")
    public ResponseEntity<String> getCurrentUser(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok("Authenticated user: " + user.getName());
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserDTO dto) {

        if (userRepository.findByName(dto.username).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        User user = new User(dto.username, passwordEncoder.encode(dto.password));
        try {
            User savedUser = userRepository.save(user);
            return ResponseEntity.ok(new UserDTO(savedUser.getName(), "[PROTECTED]"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Registration failed: " + e.getMessage());
        }
    }


        @PostMapping("/login")
        public String login (@RequestBody UserDTO dto){
            User user = userRepository.findByName(dto.username).orElseThrow();

            if (!passwordEncoder.matches(dto.password, user.getPassword())) {
                return null;
            }

            Algorithm algorithm = Algorithm.HMAC256("secretsecretsecret");

            return JWT.create()
                    .withIssuer("auth0")
                    .withSubject(user.getId().toString())
                    .withClaim("loginDate", new Date())
                    .withExpiresAt(Instant.now().plus(15, ChronoUnit.MINUTES))
                    .sign(algorithm);
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class UserDTO {
            public String username;
            public String password;
        }
    }