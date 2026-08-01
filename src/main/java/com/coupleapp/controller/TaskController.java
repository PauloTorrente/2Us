package com.coupleapp.controller;

import com.coupleapp.dto.TaskDTOs.*;
import com.coupleapp.entity.User;
import com.coupleapp.service.impl.TaskService;
import com.coupleapp.util.UserResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Domestic task management")
public class TaskController {

    private final TaskService taskService;
    private final UserResolver userResolver;

    @PostMapping
    @Operation(summary = "Create a new task")
    public ResponseEntity<TaskResponse> createTask(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateTaskRequest request
    ) {
        User user = userResolver.resolveWithCouple(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.createTask(request, user));
    }

    @GetMapping
    @Operation(summary = "Get all tasks for the couple")
    public ResponseEntity<List<TaskResponse>> getTasks(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userResolver.resolveWithCouple(userDetails);
        return ResponseEntity.ok(taskService.getTasksByCouple(user));
    }

    // Marks a task as done. For AGREED/RANDOM tasks this automatically spawns the next
    // occurrence (same partner if AGREED, re-rolled partner if RANDOM) — the recurring cycle.
    @PatchMapping("/{taskId}/complete")
    @Operation(summary = "Complete a task (spawns the next occurrence if AGREED/RANDOM)")
    public ResponseEntity<TaskResponse> completeTask(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long taskId
    ) {
        User user = userResolver.resolveWithCouple(userDetails);
        return ResponseEntity.ok(taskService.completeTask(taskId, user));
    }
}
