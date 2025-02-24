package com.example.demo.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/test-auth")
    public ResponseEntity<String> testAuth(@AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok("Autentiserad som: " + user.getName() +
                ", Provider: " + user.getProvider());
    }

    @GetMapping("/current-user")
    public ResponseEntity<String> getCurrentUser(@AuthenticationPrincipal UserEntity user) {
        return userService.getCurrentUser(user);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserDTO dto) {
        return userService.registerUser(dto);
    }

        @PostMapping("/login")
        public ResponseEntity<?> login (@RequestBody UserDTO dto){
            return userService.login(dto);
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class UserDTO {
            public String username;
            public String password;
        }
    }