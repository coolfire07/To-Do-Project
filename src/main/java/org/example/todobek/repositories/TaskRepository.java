package org.example.todobek.repositories;

import org.example.todobek.entities.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    Optional<Task> findById(Long id);
    public List<Task> findByCompletionDate(LocalDate completionDate);


}