package com.example.ChronoTask.repository;

import com.example.ChronoTask.model.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    @Query("SELECT n FROM NotificationEntity n " +
            "JOIN com.example.ChronoTask.model.Task t ON n.taskId = t.id " +
            "WHERE t.userId = :userId " +
            "  AND n.status = 'PENDING' " +
            "  AND n.scheduledTime <= CURRENT_TIMESTAMP")
    List<NotificationEntity> findPendingNotifications(@Param("userId") Long userId);

    void deleteByTaskIdAndStatus(Long taskId, String status);
}
