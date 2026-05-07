package com.coupleapp.service.impl;

import com.coupleapp.dto.TaskDTOs.*;
import com.coupleapp.entity.Task;
import com.coupleapp.entity.User;
import com.coupleapp.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    @Transactional
    public TaskResponse createTask(CreateTaskRequest request, User user) {
        if (user.getCouple() == null) {
            throw new RuntimeException("User must be in a couple to create tasks");
        }

        Task task = Task.builder()
                .description(request.description())
                .assignment(request.assignment() != null ? request.assignment() : Task.TaskAssignment.MANUAL)
                .couple(user.getCouple())
                .status(Task.TaskStatus.PENDING)
                .build();

        task = taskRepository.save(task);

        return mapToResponse(task);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByCouple(User user) {
        if (user.getCouple() == null) {
            throw new RuntimeException("User must be in a couple");
        }

        return taskRepository.findByCoupleId(user.getCouple().getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private TaskResponse mapToResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getDescription(),
                task.getAssignment(),
                task.getStatus(),
                task.getAssignedTo() != null ? task.getAssignedTo().getName() : null,
                task.getCreatedAt(),
                task.getCompletedAt()
        );
    }

    /**
     * Seeds default household tasks when a couple is created.
     * This helps new users get started with common tasks.
     */
    @Transactional
    public void seedDefaultTasks(com.coupleapp.entity.Couple couple) {
        List<String> defaultTasks = List.of(
                "Lavar louça",
                "Passar roupa",
                "Limpar banheiro",
                "Fazer compras do mercado",
                "Cozinhar jantar",
                "Tirar lixo",
                "Limpar quarto",
                "Aspirar sala"
        );

        for (String description : defaultTasks) {
            Task task = Task.builder()
                    .description(description)
                    .assignment(Task.TaskAssignment.RANDOM)
                    .couple(couple)
                    .status(Task.TaskStatus.PENDING)
                    .build();
            taskRepository.save(task);
        }
    }
}
