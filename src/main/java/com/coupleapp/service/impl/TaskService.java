package com.coupleapp.service.impl;

import com.coupleapp.dto.TaskDTOs.*;
import com.coupleapp.entity.Couple;
import com.coupleapp.entity.Notification.NotificationType;
import com.coupleapp.entity.Task;
import com.coupleapp.entity.User;
import com.coupleapp.exception.ForbiddenException;
import com.coupleapp.exception.NotFoundException;
import com.coupleapp.repository.TaskRepository;
import com.coupleapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    private static final SecureRandom RANDOM = new SecureRandom();

    // Couple membership is already enforced by UserResolver.resolveWithCouple() in the controller.
    @Transactional
    public TaskResponse createTask(CreateTaskRequest request, User user) {
        Task.TaskAssignment assignment = request.assignment() != null ? request.assignment() : Task.TaskAssignment.MANUAL;
        User assignedTo = resolveAssignee(assignment, request.assignedToUserId(), user.getCouple());

        Task task = Task.builder()
                .description(request.description())
                .assignment(assignment)
                .assignedTo(assignedTo)
                .couple(user.getCouple())
                .status(Task.TaskStatus.PENDING)
                .build();

        task = taskRepository.save(task);

        if (assignedTo != null) {
            notificationService.create(assignedTo,
                    "Nova tarefa pra você: " + task.getDescription(),
                    NotificationType.TASK_ASSIGNED, task.getId());
        }

        return mapToResponse(task);
    }

    // AGREED and RANDOM tasks are recurring by design: completing one spawns the next occurrence
    // right away (AGREED keeps the same partner, RANDOM re-rolls).
    @Transactional
    public TaskResponse completeTask(Long taskId, User user) {
        Task task = getTaskBelongingToCouple(taskId, user.getCouple().getId());

        task.setStatus(Task.TaskStatus.COMPLETED);
        task.setCompletedAt(java.time.LocalDateTime.now());
        task = taskRepository.save(task);

        if (task.getAssignment() == Task.TaskAssignment.AGREED || task.getAssignment() == Task.TaskAssignment.RANDOM) {
            spawnNextOccurrence(task);
        }

        return mapToResponse(task);
    }

    private void spawnNextOccurrence(Task completed) {
        User nextAssignee = completed.getAssignment() == Task.TaskAssignment.RANDOM
                ? pickRandomPartner(completed.getCouple())
                : completed.getAssignedTo();

        Task next = Task.builder()
                .description(completed.getDescription())
                .assignment(completed.getAssignment())
                .assignedTo(nextAssignee)
                .couple(completed.getCouple())
                .status(Task.TaskStatus.PENDING)
                .build();

        taskRepository.save(next);

        if (nextAssignee != null) {
            notificationService.create(nextAssignee,
                    "Sua vez: " + next.getDescription(),
                    NotificationType.TASK_ASSIGNED, next.getId());
        }
    }

    // Determines who a task should be assigned to at creation time based on its distribution mode.
    private User resolveAssignee(Task.TaskAssignment assignment, Long assignedToUserId, Couple couple) {
        return switch (assignment) {
            case RANDOM -> pickRandomPartner(couple);
            case AGREED -> {
                if (assignedToUserId == null) {
                    throw new ForbiddenException("AGREED tasks require the couple to pick who it belongs to (assignedToUserId)");
                }
                yield validatePartnerOfCouple(assignedToUserId, couple);
            }
            case MANUAL -> assignedToUserId != null ? validatePartnerOfCouple(assignedToUserId, couple) : null;
        };
    }

    // Randomly picks one of the couple's two partners — the actual "sorteio" behind RANDOM tasks.
    private User pickRandomPartner(Couple couple) {
        List<User> partners = userRepository.findByCoupleId(couple.getId());
        if (partners.isEmpty()) {
            return null;
        }
        return partners.get(RANDOM.nextInt(partners.size()));
    }

    private User validatePartnerOfCouple(Long userId, Couple couple) {
        User candidate = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        if (candidate.getCouple() == null || !candidate.getCouple().getId().equals(couple.getId())) {
            throw new ForbiddenException("assignedToUserId must be one of the couple's partners");
        }
        return candidate;
    }

    private Task getTaskBelongingToCouple(Long taskId, Long coupleId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found: " + taskId));
        if (!task.getCouple().getId().equals(coupleId)) {
            throw new ForbiddenException("This task does not belong to your couple");
        }
        return task;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByCouple(User user) {
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

    // Seeds a starter set of household tasks so a new couple isn't staring at an empty list.
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
                    .assignedTo(pickRandomPartner(couple))
                    .couple(couple)
                    .status(Task.TaskStatus.PENDING)
                    .build();
            taskRepository.save(task);
        }
    }
}
