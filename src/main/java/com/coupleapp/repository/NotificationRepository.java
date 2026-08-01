package com.coupleapp.repository;

import com.coupleapp.entity.Notification;
import com.coupleapp.entity.Notification.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Unread notifications for a user — shown as badge count in the app
    List<Notification> findByRecipientIdAndReadFalseOrderByCreatedAtDesc(Long userId);

    // Full notification history for a user
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long userId);

    // Count of unread — used for the notification bell badge number
    long countByRecipientIdAndReadFalse(Long userId);

    // Idempotency check for the reminder scheduler: has this exact reminder already fired today?
    boolean existsByRecipientIdAndReferenceIdAndTypeAndCreatedAtAfter(
            Long recipientId, Long referenceId, NotificationType type, LocalDateTime after);
}
