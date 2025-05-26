package com.example.ChronoTask.service;

import com.example.ChronoTask.model.Task;
import com.example.ChronoTask.model.TaskArchive;
import com.example.ChronoTask.model.TaskReschedules;
import com.example.ChronoTask.repository.TaskRepository;
import com.example.ChronoTask.repository.TaskArchiveRepository;
import com.example.ChronoTask.repository.TaskReschedulesRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.*;
import java.util.Comparator;
import java.util.List;

/**
 * Сервис для работы с задачами:
 * - создание/редактирование,
 * - планирование уведомлений,
 * - подтверждение выполнения,
 * - перенос,
 * - архивирование,
 * - получение архивированных задач и истории переноса.
 */
@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskArchiveRepository taskArchiveRepository;
    private final TaskReschedulesRepository taskReschedulesRepository;
    private final NotificationService notificationService;

    public TaskService(TaskRepository taskRepository,
                       TaskArchiveRepository taskArchiveRepository,
                       TaskReschedulesRepository taskReschedulesRepository,
                       NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.taskArchiveRepository = taskArchiveRepository;
        this.taskReschedulesRepository = taskReschedulesRepository;
        this.notificationService = notificationService;
    }

    public Task createTask(Long userId,
                           String title,
                           String description,
                           LocalDate date,
                           LocalTime time,
                           String priority) {
        Task task = new Task();
        task.setUserId(userId);
        task.setTitle(title);
        task.setDescription(description);
        task.setTaskDate(date);


        LocalDateTime scheduledDateTime = LocalDateTime.of(date, time);
        task.setTaskTime(Timestamp.valueOf(scheduledDateTime));

        task.setPriority(priority.toUpperCase());
        task.setStatus("ACTIVE");
        task.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));

        taskRepository.save(task);


        String creationMessage = String.format(
                "Вы создали новую задачу '%s'. Дата создания: %s",
                task.getTitle(),
                task.getCreatedAt().toString()
        );
        notificationService.createPendingNotification(
                task.getId(),
                Timestamp.valueOf(LocalDateTime.now()),
                creationMessage
        );


        scheduleNotifications(task);

        return task;
    }


    public void scheduleNotifications(Task task) {
        LocalDateTime scheduledDateTime = LocalDateTime.of(task.getTaskDate(), task.getTaskTime().toLocalDateTime().toLocalTime());
        LocalDateTime now = LocalDateTime.now();


        if (scheduledDateTime.isAfter(now)) {
            long remainingHours = Duration.between(now, scheduledDateTime).toHours();
            boolean hasReschedules = hasPossibleRescheduleDates(task.getId());

            if ("HIGH".equalsIgnoreCase(task.getPriority()) || (!hasReschedules && "LOW".equalsIgnoreCase(task.getPriority()))) {
                if (remainingHours >= 24) {
                    LocalDateTime notif24 = scheduledDateTime.minusHours(24);
                    if (notif24.isAfter(now)) {
                        notificationService.createPendingNotification(
                                task.getId(),
                                Timestamp.valueOf(notif24),
                                "До выполнения задачи '" + task.getTitle() + "' осталось 24 часа."
                        );
                    }
                }
                if (remainingHours >= 6) {
                    LocalDateTime notif6 = scheduledDateTime.minusHours(6);
                    if (notif6.isAfter(now)) {
                        notificationService.createPendingNotification(
                                task.getId(),
                                Timestamp.valueOf(notif6),
                                "До выполнения задачи '" + task.getTitle() + "' осталось 6 часов."
                        );
                    }
                }
                if (remainingHours >= 1) {
                    LocalDateTime notif1 = scheduledDateTime.minusHours(1);
                    if (notif1.isAfter(now)) {
                        notificationService.createPendingNotification(
                                task.getId(),
                                Timestamp.valueOf(notif1),
                                "До выполнения задачи '" + task.getTitle() + "' остался 1 час."
                        );
                    }
                }
            } else if ("LOW".equalsIgnoreCase(task.getPriority()) && hasReschedules) {
                List<TaskReschedules> possibleDates = taskReschedulesRepository.findByTaskId(task.getId());
                possibleDates.sort(Comparator.comparing(TaskReschedules::getNewDateTime));
                LocalDateTime nowLdt = LocalDateTime.now();
                possibleDates.removeIf(r -> r.getNewDateTime().isBefore(nowLdt));
                if (!possibleDates.isEmpty()) {
                    LocalDateTime nextDt = possibleDates.get(0).getNewDateTime();
                    notificationService.createPendingNotification(
                            task.getId(),
                            Timestamp.valueOf(scheduledDateTime),
                            String.format(
                                    "Задача '%s' начинается. Подтвердите выполнение в течение часа, иначе она будет перенесена на %s.",
                                    task.getTitle(),
                                    nextDt.toString()
                            )
                    );
                } else {
                    notificationService.createPendingNotification(
                            task.getId(),
                            Timestamp.valueOf(scheduledDateTime),
                            String.format(
                                    "Задача '%s' начинается. Подтвердите выполнение в течение часа, иначе она будет архивирована.",
                                    task.getTitle()
                            )
                    );
                }
            }
        }
    }

    public boolean hasPossibleRescheduleDates(Long taskId) {
        return !taskReschedulesRepository.findByTaskId(taskId).isEmpty();
    }

    public List<Task> findTasksByDateAndUser(LocalDate date, Long userId) {
        return taskRepository.findByUserIdAndTaskDate(userId, date);
    }

    public List<Task> findTasksByPeriodAndUser(LocalDate start, LocalDate end, Long userId) {
        return taskRepository.findByUserIdAndTaskDateBetween(userId, start, end);
    }

    public void confirmTask(Task task) {
        task.setStatus("COMPLETED");
        taskRepository.save(task);
        archiveTask(task, "COMPLETED");
    }

    public void rescheduleTask(Task task, LocalDate newDate, LocalTime newTime, String reason) {
        TaskReschedules res = new TaskReschedules();
        res.setTaskId(task.getId());
        res.setNewDate(newDate);
        if (newTime != null) {
            LocalDateTime newDateTime = LocalDateTime.of(newDate, newTime);
            res.setNewTime(Timestamp.valueOf(newDateTime));
        }
        res.setNewPriority(task.getPriority());
        res.setRescheduleReason(reason);
        res.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        taskReschedulesRepository.save(res);

        task.setTaskDate(newDate);
        if (newTime != null) {
            task.setTaskTime(Timestamp.valueOf(LocalDateTime.of(newDate, newTime)));
        }
        task.setStatus("RESCHEDULED");
        taskRepository.save(task);

        notificationService.deletePendingNotificationsByTaskId(task.getId());
        scheduleNotifications(task);
    }

    public void archiveTask(Task task, String finalStatus) {
        TaskArchive archive = new TaskArchive();
        archive.setTaskId(task.getId());
        archive.setArchivedAt(Timestamp.valueOf(LocalDateTime.now()));
        archive.setStatus(finalStatus);
        archive.setNotes("Задача архивирована (автоматически или по подтверждению).");
        taskArchiveRepository.save(archive);
        task.setStatus("ARCHIVED");
        taskRepository.save(task);
    }


    @Scheduled(fixedRate = 300000)
    public void checkLowPriorityExpiration() {
        LocalDateTime now = LocalDateTime.now();
        List<Task> overdueTasks = taskRepository.findOverdueLowPriorityTasks(now);

        for (Task t : overdueTasks) {
            List<TaskReschedules> possibleDates = taskReschedulesRepository.findByTaskId(t.getId());

            // Удалить устаревшие даты
            possibleDates.removeIf(r -> r.getNewDateTime().isBefore(now));
            possibleDates.sort(Comparator.comparing(TaskReschedules::getNewDateTime));

            if (possibleDates.isEmpty()) {
                archiveTask(t, "AUTO_ARCHIVED");
            } else {

                TaskReschedules first = possibleDates.get(0);
                LocalDate newDate = first.getNewDate();
                LocalTime newTime = first.getNewTime() != null
                        ? first.getNewTime().toLocalDateTime().toLocalTime()
                        : LocalTime.MIDNIGHT;


                taskReschedulesRepository.delete(first);


                rescheduleTask(t, newDate, newTime, "AUTO: перенос из-за неподтверждения");
            }
        }
    }


    public List<Task> findArchivedTasksByUser(Long userId) {
        return taskRepository.findByUserIdAndStatus(userId, "ARCHIVED");
    }

    public List<TaskReschedules> getRescheduleHistory(Long taskId) {
        return taskReschedulesRepository.findByTaskId(taskId);
    }

    public void addPossibleRescheduleDate(Task task, LocalDate newDate, LocalTime newTime, String reason) {
        TaskReschedules res = new TaskReschedules();
        res.setTaskId(task.getId());
        res.setNewDate(newDate);
        if (newTime != null) {
            LocalDateTime ldt = LocalDateTime.of(newDate, newTime);
            res.setNewTime(Timestamp.valueOf(ldt));
        }
        res.setNewPriority(task.getPriority());
        res.setRescheduleReason(reason);
        res.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        taskReschedulesRepository.save(res);
    }
}
