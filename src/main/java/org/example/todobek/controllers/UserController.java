package org.example.todobek.controllers;

import org.example.todobek.dto.RegisterRequest;
import org.example.todobek.entities.User;
import org.example.todobek.jwt.JwtUtil;
import org.example.todobek.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody User loginDetails) {
        Optional<User> userOptional = userRepository.findByUsername(loginDetails.getUsername());

        if (userOptional.isPresent() && passwordEncoder.matches(loginDetails.getPassword(), userOptional.get().getPassword())) {
            String token = JwtUtil.generateToken(userOptional.get().getUsername());
            logger.info("User logged in: {}", loginDetails.getUsername());

            return ResponseEntity.ok("{\"token\": \"" + token + "\"}");
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
    }

    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody RegisterRequest registerRequest) {
        logger.info("Пришел запрос на регистрацию: {}", registerRequest.getUsername());

        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            logger.error("Ошибка: пароли не совпадают");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"message\": \"Passwords do not match\"}");
        }

        Optional<User> existingUser = userRepository.findByUsername(registerRequest.getUsername());
        if (existingUser.isPresent()) {
            logger.error("Ошибка: пользователь уже существует");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"message\": \"Username already taken\"}");
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        userRepository.save(user);
        logger.info("Регистрация успешна");

        return ResponseEntity.status(HttpStatus.CREATED).body("{\"message\": \"User created successfully\"}");
    }
}
