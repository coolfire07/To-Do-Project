package org.example.todobek.services;

import org.example.todobek.entities.Task;
import org.example.todobek.entities.TaskStatus;
import org.example.todobek.repositories.TaskRepository;
import org.example.todobek.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserService userService;

    public TaskService(TaskRepository repository, UserService userService) {
        this.taskRepository = repository;
        this.userService = userService;
    }

    public List<Task> getTasks(TaskStatus status, LocalDate completionDate, String keyword, Long userId, LocalDate creationDate, String sortBy) {
        return taskRepository.findAll().stream()
                .filter(task -> task.getUser().getId().equals(userId))
                .filter(task -> status == null || task.getStatus() == status)
                .filter(task -> completionDate == null || task.getCompletionDate() != null && task.getCompletionDate().equals(completionDate))
                .filter(task -> creationDate == null || task.getCreationDate() != null && task.getCreationDate().equals(creationDate))
                .filter(task -> keyword == null || task.getTaskName().toLowerCase().contains(keyword.toLowerCase()))
                .sorted((task1, task2) -> {
                    if ("creationDate".equals(sortBy)) {
                        return task1.getCreationDate().compareTo(task2.getCreationDate());
                    } else if ("completionDate".equals(sortBy)) {
                        return task1.getCompletionDate().compareTo(task2.getCompletionDate());
                    }
                    return 0;
                })
                .toList();
    }

    public Task saveTask(Task task) {
        return taskRepository.save(task);
    }

    public boolean deleteTask(Long id) {
        Optional<Task> task = taskRepository.findById(id);
        if (task.isPresent()) {
            taskRepository.delete(task.get());
            return true;
        } else {
            return false;
        }
    }

    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    public Task updateTask(Long id, Task task) {
        Optional<Task> existingTask = taskRepository.findById(id);
        if (existingTask.isPresent()) {
            Task taskToUpdate = existingTask.get();
            taskToUpdate.setTaskName(task.getTaskName());
            taskToUpdate.setDescription(task.getDescription());
            taskToUpdate.setCompletionDate(task.getCompletionDate());
            taskToUpdate.setStatus(task.getStatus());
            return taskRepository.save(taskToUpdate);
        }
        return null;
    }
}