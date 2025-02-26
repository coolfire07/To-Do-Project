package org.example.todobek.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.example.todobek.entities.Task;
import org.example.todobek.entities.TaskStatus;
import org.example.todobek.entities.User;
import org.example.todobek.jwt.JwtUtil;
import org.example.todobek.repositories.UserRepository;
import org.example.todobek.services.TaskService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;
    private final UserRepository userRepository;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    public TaskController (TaskService taskService, UserRepository userRepository) {
        this.taskService = taskService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<Task>> getTasksJson(@RequestParam(required = false) TaskStatus status,
                                                   @RequestParam(required = false) String date,
                                                   @RequestParam(required = false) String keyword,
                                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate creationDate,
                                                   @RequestParam(required = false) String sortBy,
                                                   HttpServletRequest request) {

        LocalDate completionDate = null;
        if (date != null) {
            completionDate = LocalDate.parse(date);
        }

        String authorization = request.getHeader(AUTHORIZATION_HEADER);

        if (authorization != null && authorization.startsWith(BEARER_PREFIX)) {
            String token = authorization.substring(7);
            String username = JwtUtil.extractUsername(token);

            if (!JwtUtil.validateToken(token, JwtUtil.extractUsername(token))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, null);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            try {
                Optional<User> userOptional = userRepository.findByUsername(username);
                if (userOptional.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                }
                User user = userOptional.get();
                Long userId = user.getId();

                if (userId == null) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                }
                List<Task> tasks = taskService.getTasks(status, completionDate, keyword, userId, creationDate, sortBy);
                return ResponseEntity.ok(tasks);

            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task, HttpServletRequest request) {
        String authorization = request.getHeader(AUTHORIZATION_HEADER);

        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authorization.substring(7);
        String username = JwtUtil.extractUsername(token);

        if (!JwtUtil.validateToken(token, username)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        User user = userOptional.get();
        task.setUser(user);
        Task savedTask = taskService.saveTask(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTask);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id, HttpServletRequest request) {
        String authorization = request.getHeader(AUTHORIZATION_HEADER);

        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authorization.substring(7);
        String username = JwtUtil.extractUsername(token);

        if (!JwtUtil.validateToken(token, username)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        User user = userOptional.get();
        Optional<Task> taskOptional = taskService.getTaskById(id);

        if (taskOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Task task = taskOptional.get();

        if (!task.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(task);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task updatedTask, HttpServletRequest request) {
        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String token = authorization.substring(7);
        String username = JwtUtil.extractUsername(token);

        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        User user = userOptional.get();
        Optional<Task> taskOptional = taskService.getTaskById(id);
        if (taskOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Task existingTask = taskOptional.get();
        if (!existingTask.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        existingTask.setTaskName(updatedTask.getTaskName());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setStatus(updatedTask.getStatus());
        existingTask.setCompletionDate(updatedTask.getCompletionDate());

        Task savedTask = taskService.saveTask(existingTask);
        return ResponseEntity.ok(savedTask);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteTask(@PathVariable Long id) {
        boolean isDeleted = taskService.deleteTask(id);

        if (isDeleted) {
            return ResponseEntity.ok(Collections.singletonMap("message", "Task deleted successfully."));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("message", "Task not found."));
        }
    }

}