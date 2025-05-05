package com.example.ChronoTask.model;

import jakarta.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDate;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Идентификатор пользователя
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "task_date", nullable = false)
    private LocalDate taskDate;

    // Полное время выполнения задачи
    @Column(name = "task_time", nullable = false)
    private Timestamp taskTime;

    @Column(name = "priority", nullable = false, length = 10)
    private String priority;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    // Флаги для уведомлений
    @Column(name = "notification_24_created", nullable = false)
    private boolean notification24Created = false;

    @Column(name = "notification_6_created", nullable = false)
    private boolean notification6Created = false;

    @Column(name = "notification_1_created", nullable = false)
    private boolean notification1Created = false;

    // Геттеры и сеттеры

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public LocalDate getTaskDate() {
        return taskDate;
    }
    public void setTaskDate(LocalDate taskDate) {
        this.taskDate = taskDate;
    }
    public Timestamp getTaskTime() {
        return taskTime;
    }
    public void setTaskTime(Timestamp taskTime) {
        this.taskTime = taskTime;
    }
    public String getPriority() {
        return priority;
    }
    public void setPriority(String priority) {
        this.priority = priority;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    public boolean isNotification24Created() {
        return notification24Created;
    }
    public void setNotification24Created(boolean notification24Created) {
        this.notification24Created = notification24Created;
    }
    public boolean isNotification6Created() {
        return notification6Created;
    }
    public void setNotification6Created(boolean notification6Created) {
        this.notification6Created = notification6Created;
    }
    public boolean isNotification1Created() {
        return notification1Created;
    }
    public void setNotification1Created(boolean notification1Created) {
        this.notification1Created = notification1Created;
    }
}