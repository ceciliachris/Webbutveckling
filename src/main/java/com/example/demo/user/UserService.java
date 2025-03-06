package com.example.demo.user;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;


/**
 * Service-klass för att hantera användare.
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Hämtar den inloggade användaren.
     * @param user Den autentiserade användaren.
     * @return ResponseEntity med användarnamnet om användaren är inloggad,
     * annars felkod 401 Unauthorized.
     */
    public ResponseEntity<String> getCurrentUserString(UserEntity user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok("Authenticated user: " + user.getName());
    }

    /**
     * Hämtar en användare baserat på ID.
     * @param userId användarens ID.
     * @return Optional med användaren om den hittas.
     */
    public Optional<UserEntity> getUserById(UUID userId) {
        return userRepository.findById(userId);
    }

    /**
     * Hämtar alla användare för administrativa ändamål.
     * @return
     */
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Registrerar en ny användare i systemet.
     * @param dto DTO-objekt som innehåller användarnamn och lösenord.
     * @return ResponseEntity med antingen den registrerade användaren eller ett felmeddelande.
     */
    public ResponseEntity<?> registerUser(UserController.UserDTO dto) {
        if (userRepository.findByName(dto.username).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        UserEntity user = new UserEntity(dto.username, passwordEncoder.encode(dto.password));
        try {
            UserEntity savedUser = userRepository.save(user);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Registration failed: " + e.getMessage());
        }
    }

    /**
     * Autentiserar en användare och returnerar en JWT-token vid lyckad inloggning.
     * @param dto DTO-objekt med användarnamn och lösenord.
     * @return En JWT-token om inloggningen lyckas, annars null.
     */
    public ResponseEntity<?> login(UserController.UserDTO dto) {
        Optional<UserEntity> optionalUser = userRepository.findByName(dto.username);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }

        UserEntity user = optionalUser.get();
        if(!passwordEncoder.matches(dto.password, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }

        Algorithm algorithm = Algorithm.HMAC256("secretsecretsecret");
        String token = JWT.create()
                .withIssuer("auth0")
                .withSubject(user.getId().toString())
                .withClaim("loginDate", new Date())
                .withExpiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .sign(algorithm);

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("userId", user.getId());
        response.put("user", user);

        return ResponseEntity.ok(response);
    }
}