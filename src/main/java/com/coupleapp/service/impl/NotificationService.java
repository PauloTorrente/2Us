package com.coupleapp.service.impl;

import com.coupleapp.dto.NotificationDTOs.NotificationResponse;
import com.coupleapp.entity.Notification;
import com.coupleapp.entity.Notification.NotificationType;
import com.coupleapp.entity.User;
import com.coupleapp.exception.ForbiddenException;
import com.coupleapp.exception.NotFoundException;
import com.coupleapp.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// Creates and delivers in-app notifications (task assignments, calendar reminders, etc.).
// This is the "motor" behind reminders — entities alone don't send anything without this.
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public Notification create(User recipient, String message, NotificationType type, Long referenceId) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .message(message)
                .type(type)
                .referenceId(referenceId)
                .build();
        return notificationRepository.save(notification);
    }

    // Idempotency guard so a scheduled job can run more than once a day without spamming the couple.
    public boolean alreadySentToday(Long recipientId, Long referenceId, NotificationType type) {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        return notificationRepository.existsByRecipientIdAndReferenceIdAndTypeAndCreatedAtAfter(
                recipientId, referenceId, type, startOfToday
        );
    }

    public List<NotificationResponse> getUnread(User user) {
        return notificationRepository.findByRecipientIdAndReadFalseOrderByCreatedAtDesc(user.getId())
                .stream().map(this::mapToResponse).toList();
    }

    public List<NotificationResponse> getHistory(User user) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(user.getId())
                .stream().map(this::mapToResponse).toList();
    }

    public long getUnreadCount(User user) {
        return notificationRepository.countByRecipientIdAndReadFalse(user.getId());
    }

    @Transactional
    public void markRead(User user, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("Notification not found: " + notificationId));
        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new ForbiddenException("This notification does not belong to you");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    private NotificationResponse mapToResponse(Notification n) {
        return new NotificationResponse(
                n.getId(), n.getMessage(), n.getType(), n.getReferenceId(),
                n.getRead(), n.getCreatedAt()
        );
    }
}
