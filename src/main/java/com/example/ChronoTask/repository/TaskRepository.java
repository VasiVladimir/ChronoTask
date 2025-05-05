package com.example.ChronoTask.repository;

import com.example.ChronoTask.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUserIdAndTaskDate(Long userId, LocalDate date);

    List<Task> findByUserIdAndTaskDateBetween(Long userId, LocalDate start, LocalDate end);

    List<Task> findByUserIdAndStatus(Long userId, String status);

    @Query(value = "SELECT * FROM tasks t " +
            "WHERE t.priority = 'LOW' " +
            "  AND (t.status = 'ACTIVE' OR t.status = 'RESCHEDULED') " +
            "  AND (t.task_date + t.task_time::interval) <= :nowTime " +
            "  AND ((t.task_date + t.task_time::interval) + INTERVAL '1 hour') < :nowTime", nativeQuery = true)
    List<Task> findOverdueLowPriorityTasks(@Param("nowTime") LocalDateTime nowTime);

    @Query(value = "SELECT * FROM tasks t " +
            "WHERE t.priority = 'LOW' " +
            "  AND (t.status = 'ACTIVE' OR t.status = 'RESCHEDULED') " +
            "  AND ((t.task_date + t.task_time::interval) + INTERVAL '3 hour') < :currentTime", nativeQuery = true)
    List<Task> findExpiredLowPriorityTasks(@Param("currentTime") LocalDateTime currentTime);
}
