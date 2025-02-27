package org.example.todobek.services;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.example.todobek.entities.Task;
import org.example.todobek.entities.TaskStatus;
import org.example.todobek.repositories.TaskRepository;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserService userService;

    private static final String CREATION_DATE = "creationDate";
    private static final String COMPLETION_DATE = "completionDate";

    @PersistenceContext
    private EntityManager entityManager;

    public TaskService(TaskRepository repository, UserService userService) {
        this.taskRepository = repository;
        this.userService = userService;
    }

    public List<Task> getTasks(TaskStatus status, LocalDate completionDate, String keyword, Long userId, LocalDate creationDate, String sortBy) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Task> criteriaQuery = criteriaBuilder.createQuery(Task.class);
        Root<Task> taskRoot = criteriaQuery.from(Task.class);

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(criteriaBuilder.equal(taskRoot.get("user").get("id"), userId));

        if (status != null) {
            predicates.add(criteriaBuilder.equal(taskRoot.get("status"), status));
        }
        if (completionDate != null) {
            predicates.add(criteriaBuilder.equal(taskRoot.get(COMPLETION_DATE), completionDate));
        }
        if (creationDate != null) {
            predicates.add(criteriaBuilder.equal(taskRoot.get(CREATION_DATE), creationDate));
        }
        if (keyword != null) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(taskRoot.get("taskName")), "%" + keyword.toLowerCase() + "%"));
        }

        criteriaQuery.select(taskRoot).where(predicates.toArray(new Predicate[0]));

        if (CREATION_DATE.equals(sortBy)) {
            criteriaQuery.orderBy(criteriaBuilder.asc(taskRoot.get(CREATION_DATE)));
        } else if (COMPLETION_DATE.equals(sortBy)) {
            criteriaQuery.orderBy(criteriaBuilder.asc(taskRoot.get(COMPLETION_DATE)));
        }

        TypedQuery<Task> query = entityManager.createQuery(criteriaQuery);
        return query.getResultList();
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