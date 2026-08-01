package com.coupleapp.dto;

import com.coupleapp.entity.Notification.NotificationType;

import java.time.LocalDateTime;

public class NotificationDTOs {

    public record NotificationResponse(
            Long id,
            String message,
            NotificationType type,
            Long referenceId,
            Boolean read,
            LocalDateTime createdAt
    ) {}
}
