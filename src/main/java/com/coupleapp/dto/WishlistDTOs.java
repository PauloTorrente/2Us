package com.coupleapp.dto;

import com.coupleapp.entity.WishlistItem.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WishlistDTOs {

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class CreateWishlistItemRequest {
        @NotBlank @Size(max = 200)
        private String title;

        @Size(max = 500)
        private String note;

        @Size(max = 500)
        private String productUrl;  // Shopee, Amazon, Steam, etc.

        @Size(max = 500)
        private String imageUrl;    // Direct image link or uploaded image URL

        private BigDecimal price;
        private WishlistSource source = WishlistSource.MANUAL;
        private Priority priority = Priority.MEDIUM;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class WishlistItemResponse {
        private Long id;
        private String title;
        private String note;
        private String productUrl;
        private String imageUrl;
        private BigDecimal price;
        private WishlistSource source;
        private Priority priority;
        private Boolean isFulfilled;
        private Long userId;
        private String userName;
        private LocalDateTime createdAt;
    }
}
