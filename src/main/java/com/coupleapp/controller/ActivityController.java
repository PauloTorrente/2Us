package com.coupleapp.controller;

import com.coupleapp.dto.ActivityDTOs.*;
import com.coupleapp.entity.User;
import com.coupleapp.service.impl.ActivityService;
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

// Shared activities the couple does together (gym, courses, walks).
// Includes a score dashboard ranking most/least completed activities.
@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
@Tag(name = "Activities", description = "Shared couple activities with performance scoring")
public class ActivityController {

    private final ActivityService activityService;
    private final UserResolver userResolver;

    // GET /api/activities — all active activities, ordered by most completed
    @GetMapping
    @Operation(summary = "Get all shared activities")
    public ResponseEntity<List<ActivityResponse>> getActivities(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userResolver.resolveWithCouple(userDetails);
        return ResponseEntity.ok(activityService.getActivities(user));
    }

    // POST /api/activities — create a new shared activity (gym, course, etc.)
    @PostMapping
    @Operation(summary = "Create a new shared activity")
    public ResponseEntity<ActivityResponse> createActivity(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateActivityRequest request) {

        User user = userResolver.resolveWithCouple(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(activityService.createActivity(user, request));
    }

    // POST /api/activities/{activityId}/log — mark an activity as done today
    @PostMapping("/{activityId}/log")
    @Operation(summary = "Log a completion for a shared activity")
    public ResponseEntity<Void> logCompletion(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long activityId,
            @RequestBody LogActivityRequest request) {

        User user = userResolver.resolveWithCouple(userDetails);
        activityService.logCompletion(user, activityId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // GET /api/activities/score?days=30 — performance dashboard: most vs least done
    @GetMapping("/score")
    @Operation(summary = "Get activity performance score (most/least done in period)")
    public ResponseEntity<ActivityScoreResponse> getScore(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "30") int days) {

        User user = userResolver.resolveWithCouple(userDetails);
        return ResponseEntity.ok(activityService.getScore(user, days));
    }
}
