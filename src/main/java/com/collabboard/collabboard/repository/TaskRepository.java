package com.collabboard.collabboard.repository;

import com.collabboard.collabboard.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByColumnIdOrderByPosition(Long columnId);
    List<Task> findByBoardIdOrderByPosition(Long boardId);
}