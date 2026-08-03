package com.coupleapp.dto;

import com.coupleapp.entity.CoupleStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
        // The couple's members (1 while waiting for the second partner, 2 once active) —
        // lets the app resolve "my color" vs "partner's color" for calendar/task color-coding.
        private List<MemberResponse> members;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class MemberResponse {
        private Long id;
        private String name;
        private String themeColor;
        private String avatarBase64;
    }

    // Sent when a user picks/changes their own accent color
    @Data @NoArgsConstructor @AllArgsConstructor
    public static class UpdateThemeColorRequest {
        @NotBlank(message = "Theme color is required")
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Theme color must be a hex value like #D9A566")
        private String themeColor;
    }

    // Sent when a user sets/changes their own profile photo — a small JPEG the client already
    // resized to ~256x256 and base64-encoded. The max length is generous for that size but
    // short enough to reject an accidental full-resolution upload.
    @Data @NoArgsConstructor @AllArgsConstructor
    public static class UpdateAvatarRequest {
        @NotBlank(message = "Avatar is required")
        @Size(max = 400_000, message = "Avatar is too large")
        private String avatarBase64;
    }
}
