package com.example.ChronoTask.repository;

import com.example.ChronoTask.model.TaskArchive;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskArchiveRepository extends JpaRepository<TaskArchive, Long> {
}