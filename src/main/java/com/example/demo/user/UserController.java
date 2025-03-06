package com.example.demo.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserModelAssembler userModelAssembler;

    @GetMapping("/test-auth")
    public ResponseEntity<UserModel> testAuth(@AuthenticationPrincipal UserEntity user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(userModelAssembler.toModel(user));
    }

    @GetMapping("/current-user")
    public ResponseEntity<UserModel> getCurrentUser(@AuthenticationPrincipal UserEntity user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(userModelAssembler.toModel(user));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserModel> getUserById(@PathVariable UUID userId) {
        return userService.getUserById(userId)
                .map(userModelAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/users")
    public ResponseEntity<CollectionModel<UserModel>> getAllUsers() {
        List<UserEntity> users = userService.getAllUsers();
        return ResponseEntity.ok(userModelAssembler.toCollectionModel(users));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserDTO dto) {
        ResponseEntity<?> response = userService.registerUser(dto);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() instanceof UserEntity) {
            UserEntity user = (UserEntity) response.getBody();
            UserModel userModel = userModelAssembler.toModel(user);
            return ResponseEntity
                    .created(userModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                    .body(userModel);
        }
        return response;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDTO dto) {
        ResponseEntity<?> response = userService.login(dto);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() instanceof Map) {

            Map<String, Object> responseBody = (Map<String, Object>) response.getBody();

            if (responseBody.containsKey("user") && responseBody.get("user") instanceof UserEntity) {
                UserEntity user = (UserEntity) responseBody.get("user");
                UserModel userModel = userModelAssembler.toModel(user);

                Map<String, Object> enhancedResponse = new HashMap<>(responseBody);
                enhancedResponse.put("user", userModel);

                return ResponseEntity.ok(enhancedResponse);
            }
        }

        return response;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class UserDTO {
            public String username;
            public String password;
        }
    }