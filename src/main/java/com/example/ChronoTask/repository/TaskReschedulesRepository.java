package com.example.ChronoTask.repository;

import com.example.ChronoTask.model.TaskReschedules;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskReschedulesRepository extends JpaRepository<TaskReschedules, Long> {

    List<TaskReschedules> findByTaskId(Long taskId);
}