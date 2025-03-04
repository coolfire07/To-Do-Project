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

    public TaskController(TaskService taskService, UserRepository userRepository) {
        this.taskService = taskService;
        this.userRepository = userRepository;
    }

    private String extractToken(HttpServletRequest request) {
        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (authorization != null && authorization.startsWith(BEARER_PREFIX)) {
            return authorization.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    private User getAuthenticatedUser(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null || !JwtUtil.validateToken(token, JwtUtil.extractUsername(token))) {
            return null; // Или выбросьте исключение
        }

        String username = JwtUtil.extractUsername(token);
        return userRepository.findByUsername(username).orElse(null);
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getTasksJson(@RequestParam(required = false) TaskStatus status,
                                                   @RequestParam(required = false) String date,
                                                   @RequestParam(required = false) String keyword,
                                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate creationDate,
                                                   @RequestParam(required = false) String sortBy,
                                                   HttpServletRequest request) {

        LocalDate completionDate = null;
        if (date != null) {
            completionDate = LocalDate.parse(date);
        }

        User user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        try {
            Long userId = user.getId();
            List<Task> tasks = taskService.getTasks(status, completionDate, keyword, userId, creationDate, sortBy);

            List<Map<String, Object>> responseTasks = tasks.stream()
                    .map(this::createResponseTask)
                    .toList();

            return ResponseEntity.ok(responseTasks);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createTask(@RequestBody Task task, HttpServletRequest request) {
        User user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        task.setUser(user);
        Task savedTask = taskService.saveTask(task);

        Map<String, Object> responseTask = createResponseTask(savedTask);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseTask);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id, HttpServletRequest request) {
        User user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

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
    public ResponseEntity<Map<String, Object>> updateTask(@PathVariable Long id, @RequestBody Task updatedTask, HttpServletRequest request) {
        User user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Task updatedExistingTask = taskService.updateTask(id, updatedTask);
        if (updatedExistingTask == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Map<String, Object> responseTask = createResponseTask(updatedExistingTask);
        return ResponseEntity.ok(responseTask);
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

    private Map<String, Object> createResponseTask(Task task) {
        Map<String, Object> taskMap = new HashMap<>();
        taskMap.put("id", task.getId());
        taskMap.put("taskName", task.getTaskName());
        taskMap.put("description", task.getDescription());
        taskMap.put("completionDate", task.getCompletionDate());
        taskMap.put("status", task.getStatus().getDisplayName());
        taskMap.put("creationDate", task.getCreationDate());
        return taskMap;
    }
}