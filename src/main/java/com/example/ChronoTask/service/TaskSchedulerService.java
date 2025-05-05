package com.example.ChronoTask.service;

import com.example.ChronoTask.model.Task;
import com.example.ChronoTask.repository.TaskRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class TaskSchedulerService {

    private final TaskRepository taskRepository;
    private final NotificationService notificationService;

    private final double EPSILON_HOURS = 0.0833;

    public TaskSchedulerService(TaskRepository taskRepository,
                                NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.notificationService = notificationService;
    }


    @Scheduled(fixedRate = 300000)
    public void checkTasksForNotifications() {
        LocalDateTime now = LocalDateTime.now();
        List<Task> tasks = taskRepository.findAll(); // Можно оптимизировать выборку только активными задачами

        for (Task task : tasks) {

            if ("COMPLETED".equalsIgnoreCase(task.getStatus()) || "ARCHIVED".equalsIgnoreCase(task.getStatus()))
                continue;

            LocalDateTime taskTime = task.getTaskTime().toLocalDateTime();
            Duration diff = Duration.between(now, taskTime);
            if (diff.isNegative()) continue; // задача уже прошла

            double hoursLeft = diff.toMinutes() / 60.0;

            // Уведомление за 24 часа
            if (!task.isNotification24Created() && Math.abs(hoursLeft - 24.0) < EPSILON_HOURS) {
                String message = String.format("До выполнения задачи '%s' осталось %d ч %d мин.",
                        task.getTitle(), diff.toHours(), diff.toMinutesPart());
                notificationService.createPendingNotification(task.getId(), Timestamp.valueOf(now), message);
                task.setNotification24Created(true);
                taskRepository.save(task);
            }
            // Уведомление за 6 часов
            if (!task.isNotification6Created() && Math.abs(hoursLeft - 6.0) < EPSILON_HOURS) {
                String message = String.format("До выполнения задачи '%s' осталось %d ч %d мин.",
                        task.getTitle(), diff.toHours(), diff.toMinutesPart());
                notificationService.createPendingNotification(task.getId(), Timestamp.valueOf(now), message);
                task.setNotification6Created(true);
                taskRepository.save(task);
            }
            // Уведомление за 1 час
            if (!task.isNotification1Created() && Math.abs(hoursLeft - 1.0) < EPSILON_HOURS) {
                String message = String.format("До выполнения задачи '%s' осталось %d ч %d мин.",
                        task.getTitle(), diff.toHours(), diff.toMinutesPart());
                notificationService.createPendingNotification(task.getId(), Timestamp.valueOf(now), message);
                task.setNotification1Created(true);
                taskRepository.save(task);
            }
        }
    }
}