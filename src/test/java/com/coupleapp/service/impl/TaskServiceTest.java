package com.coupleapp.service.impl;

import com.coupleapp.dto.TaskDTOs.CreateTaskRequest;
import com.coupleapp.dto.TaskDTOs.TaskResponse;
import com.coupleapp.entity.Couple;
import com.coupleapp.entity.Task;
import com.coupleapp.entity.User;
import com.coupleapp.exception.ForbiddenException;
import com.coupleapp.exception.NotFoundException;
import com.coupleapp.repository.TaskRepository;
import com.coupleapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;

    private TaskService taskService;

    private Couple couple;
    private User partnerA;
    private User partnerB;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(taskRepository, userRepository, notificationService);

        couple = Couple.builder().id(1L).build();
        partnerA = User.builder().id(10L).name("Ana").couple(couple).build();
        partnerB = User.builder().id(20L).name("Bruno").couple(couple).build();
    }

    @Test
    void createTask_random_assignsOneOfTheTwoPartners() {
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findByCoupleId(1L)).thenReturn(List.of(partnerA, partnerB));

        TaskResponse response = taskService.createTask(
                new CreateTaskRequest("Lavar louça", Task.TaskAssignment.RANDOM, null), partnerA);

        assertThat(response.assignedTo()).isIn("Ana", "Bruno");
        verify(notificationService).create(any(User.class), anyString(), any(), any());
    }

    @Test
    void createTask_agreedWithoutAssignedToUserId_throwsForbidden() {
        assertThatThrownBy(() -> taskService.createTask(
                new CreateTaskRequest("Pagar conta", Task.TaskAssignment.AGREED, null), partnerA))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void createTask_agreedWithValidPartner_assignsThatPartner() {
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(20L)).thenReturn(Optional.of(partnerB));

        TaskResponse response = taskService.createTask(
                new CreateTaskRequest("Pagar conta", Task.TaskAssignment.AGREED, 20L), partnerA);

        assertThat(response.assignedTo()).isEqualTo("Bruno");
    }

    @Test
    void createTask_agreedWithPartnerFromAnotherCouple_throwsForbidden() {
        User outsider = User.builder().id(99L).name("Carla").couple(Couple.builder().id(2L).build()).build();
        when(userRepository.findById(99L)).thenReturn(Optional.of(outsider));

        assertThatThrownBy(() -> taskService.createTask(
                new CreateTaskRequest("Pagar conta", Task.TaskAssignment.AGREED, 99L), partnerA))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void completeTask_randomTask_spawnsNextOccurrenceWithNewRandomAssignee() {
        Task existing = Task.builder().id(5L).description("Tirar lixo")
                .assignment(Task.TaskAssignment.RANDOM).assignedTo(partnerA)
                .couple(couple).status(Task.TaskStatus.PENDING).build();
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(userRepository.findByCoupleId(1L)).thenReturn(List.of(partnerA, partnerB));

        taskService.completeTask(5L, partnerA);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository, times(2)).save(captor.capture());

        Task completed = captor.getAllValues().get(0);
        Task spawned = captor.getAllValues().get(1);
        assertThat(completed.getStatus()).isEqualTo(Task.TaskStatus.COMPLETED);
        assertThat(spawned.getStatus()).isEqualTo(Task.TaskStatus.PENDING);
        assertThat(spawned.getAssignedTo()).isIn(partnerA, partnerB);
    }

    @Test
    void completeTask_agreedTask_spawnsNextOccurrenceWithSamePartner() {
        Task existing = Task.builder().id(6L).description("Pagar conta")
                .assignment(Task.TaskAssignment.AGREED).assignedTo(partnerB)
                .couple(couple).status(Task.TaskStatus.PENDING).build();
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskRepository.findById(6L)).thenReturn(Optional.of(existing));

        taskService.completeTask(6L, partnerA);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues().get(1).getAssignedTo()).isEqualTo(partnerB);
        verifyNoInteractions(userRepository);
    }

    @Test
    void completeTask_manualTask_doesNotSpawnNextOccurrence() {
        Task existing = Task.builder().id(7L).description("Compra avulsa")
                .assignment(Task.TaskAssignment.MANUAL).couple(couple).status(Task.TaskStatus.PENDING).build();
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskRepository.findById(7L)).thenReturn(Optional.of(existing));

        taskService.completeTask(7L, partnerA);

        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void completeTask_taskFromAnotherCouple_throwsForbidden() {
        Couple otherCouple = Couple.builder().id(2L).build();
        Task existing = Task.builder().id(8L).description("Tarefa alheia")
                .assignment(Task.TaskAssignment.MANUAL).couple(otherCouple).status(Task.TaskStatus.PENDING).build();
        when(taskRepository.findById(8L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> taskService.completeTask(8L, partnerA))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void completeTask_taskNotFound_throwsNotFound() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.completeTask(999L, partnerA))
                .isInstanceOf(NotFoundException.class);
    }
}
