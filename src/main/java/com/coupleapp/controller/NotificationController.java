package com.coupleapp.controller;

import com.coupleapp.dto.NotificationDTOs.NotificationResponse;
import com.coupleapp.entity.User;
import com.coupleapp.service.impl.NotificationService;
import com.coupleapp.util.UserResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "In-app reminders and alerts")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserResolver userResolver;

    @GetMapping
    @Operation(summary = "Full notification history for the current user")
    public ResponseEntity<List<NotificationResponse>> getHistory(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userResolver.resolve(userDetails);
        return ResponseEntity.ok(notificationService.getHistory(user));
    }

    @GetMapping("/unread")
    @Operation(summary = "Unread notifications for the current user")
    public ResponseEntity<List<NotificationResponse>> getUnread(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userResolver.resolve(userDetails);
        return ResponseEntity.ok(notificationService.getUnread(user));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Unread notification count — for the bell badge")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userResolver.resolve(userDetails);
        return ResponseEntity.ok(Map.of("count", notificationService.getUnreadCount(user)));
    }

    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<Void> markRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long notificationId) {
        User user = userResolver.resolve(userDetails);
        notificationService.markRead(user, notificationId);
        return ResponseEntity.noContent().build();
    }
}
