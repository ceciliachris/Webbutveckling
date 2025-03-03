package com.example.demo.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

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