package com.example.ChronoTask.service;

import com.example.ChronoTask.model.NotificationEntity;
import com.example.ChronoTask.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }


    public NotificationEntity createPendingNotification(Long taskId, Timestamp scheduledTime, String message) {
        NotificationEntity notification = new NotificationEntity();
        notification.setTaskId(taskId);
        notification.setScheduledTime(scheduledTime);
        notification.setMessage(message);
        notification.setStatus("PENDING");
        notification.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        return notificationRepository.save(notification);
    }


    public NotificationEntity createPendingNotification(Long taskId, String message) {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        return createPendingNotification(taskId, now, message);
    }


    public void deletePendingNotificationsByTaskId(Long taskId) {
        notificationRepository.deleteByTaskIdAndStatus(taskId, "PENDING");
    }


    public void markAsRead(NotificationEntity notification) {
        notification.setStatus("READ");
        notificationRepository.save(notification);
    }


    public List<NotificationEntity> findAllPendingByUser(Long userId) {
        return notificationRepository.findPendingNotifications(userId);
    }
}
