package com.coupleapp.dto;

import com.coupleapp.entity.CoupleStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CoupleDTOs {

    // Sent when the first partner creates a new couple
    @Data @NoArgsConstructor @AllArgsConstructor
    public static class CreateCoupleRequest {
        private String coupleName;
        private LocalDate relationshipStartDate;
        // True if they know the exact day. False = we remind on the 1st of that month.
        private Boolean exactDateKnown = true;
    }

    // Sent when the second partner accepts the invite using the shared code
    @Data @NoArgsConstructor @AllArgsConstructor
    public static class JoinCoupleRequest {
        @NotBlank(message = "Invite code is required")
        private String inviteCode;
    }

    // Returned after creating or joining a couple
    @Data @NoArgsConstructor @AllArgsConstructor
    public static class CoupleResponse {
        private Long id;
        private String coupleName;
        private LocalDate relationshipStartDate;
        private Boolean exactDateKnown;
        private CoupleStatus status;
        private String inviteCode; // Only present for PENDING_INVITE — null after second partner joins
        private LocalDateTime createdAt;
    }
}
