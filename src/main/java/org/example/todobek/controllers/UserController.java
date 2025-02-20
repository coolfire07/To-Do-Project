package org.example.todobek.controllers;

import org.example.todobek.dto.RegisterRequest;
import org.example.todobek.entities.User;
import org.example.todobek.jwt.JwtUtil;
import org.example.todobek.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginDetails) {
        Optional<User> userOptional = userRepository.findByUsername(loginDetails.getUsername());

        if (userOptional.isPresent() && passwordEncoder.matches(loginDetails.getPassword(), userOptional.get().getPassword())) {
            String token = JwtUtil.generateToken(userOptional.get().getUsername());
            System.out.println("User logged in: " + loginDetails.getUsername());

            return ResponseEntity.ok("{\"token\": \"" + token + "\"}");
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        System.out.println("Пришел запрос на регистрацию: " + registerRequest.getUsername());

        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            System.out.println("Ошибка: пароли не совпадают");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"message\": \"Passwords do not match\"}");
        }

        Optional<User> existingUser = userRepository.findByUsername(registerRequest.getUsername());
        if (existingUser.isPresent()) {
            System.out.println("Ошибка: пользователь уже существует");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"message\": \"Username already taken\"}");
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        userRepository.save(user);
        System.out.println("Регистрация успешна");

        return ResponseEntity.status(HttpStatus.CREATED).body("{\"message\": \"User created successfully\"}");
    }
}
