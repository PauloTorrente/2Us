package com.coupleapp.repository;

import com.coupleapp.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByCoupleId(Long coupleId);
    List<Task> findByCoupleIdAndStatus(Long coupleId, Task.TaskStatus status);
}
