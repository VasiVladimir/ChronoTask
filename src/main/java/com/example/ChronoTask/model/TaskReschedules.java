package com.example.ChronoTask.model;

import jakarta.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "task_reschedules")
public class TaskReschedules {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long taskId;

    private LocalDate newDate;


    private Timestamp newTime;

    private String newPriority;

    private String rescheduleReason;

    private Timestamp createdAt;



    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getTaskId() {
        return taskId;
    }
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }
    public LocalDate getNewDate() {
        return newDate;
    }
    public void setNewDate(LocalDate newDate) {
        this.newDate = newDate;
    }
    public Timestamp getNewTime() {
        return newTime;
    }
    public void setNewTime(Timestamp newTime) {
        this.newTime = newTime;
    }
    public String getNewPriority() {
        return newPriority;
    }
    public void setNewPriority(String newPriority) {
        this.newPriority = newPriority;
    }
    public String getRescheduleReason() {
        return rescheduleReason;
    }
    public void setRescheduleReason(String rescheduleReason) {
        this.rescheduleReason = rescheduleReason;
    }
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Transient
    public LocalDateTime getNewDateTime() {
        LocalTime lt = (newTime != null) ? newTime.toLocalDateTime().toLocalTime() : LocalTime.MIDNIGHT;
        return LocalDateTime.of(newDate, lt);
    }
}