package com.coupleapp.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class SuggestionDTOs {

    // --- Place Suggestion Request/Response ---

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class PlaceSuggestionRequest {
        @NotNull
        @DecimalMin("-90.0") @DecimalMax("90.0")
        private Double latitude;

        @NotNull
        @DecimalMin("-180.0") @DecimalMax("180.0")
        private Double longitude;

        // Business type: "restaurant", "cafe", "bakery", "bar", etc.
        @Size(max = 50)
        private String businessType;

        // Optional: override couple's default max distance for this search
        @Min(100) @Max(50000)
        private Integer maxDistanceMeters;

        // Optional: filter by cuisine (e.g., "italian", "japanese")
        @Size(max = 50)
        private String cuisine;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class PlaceSuggestionResponse {
        private String placeId;  // Google Place ID
        private String name;
        private String address;
        private Double latitude;
        private Double longitude;
        private Integer distanceMeters;
        
        // Google Places data
        private Double googleRating;  // 0.0 - 5.0
        private Integer googleReviewCount;
        private Integer priceLevel;  // 1-4
        private List<String> types;  // ["restaurant", "food", "point_of_interest"]
        private String photoReference;  // Reference to fetch photo from Google
        
        // TripAdvisor data (enriched)
        private String tripAdvisorId;
        private Double tripAdvisorRating;  // 0.0 - 5.0
        private Integer tripAdvisorReviewCount;
        private String tripAdvisorUrl;
        
        // Combined score for ranking
        private Double combinedScore;  // Weighted average of both platforms
        
        // Business hours
        private Boolean openNow;
        private String openingHours;
    }

    // --- Preferences ---

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class UpdatePreferencesRequest {
        @Min(100) @Max(50000)
        private Integer maxDistanceMeters;

        private Set<String> cuisineTypes;  // ["italian", "japanese", "mexican"]
        private Set<String> businessTypes;  // ["restaurant", "cafe", "bakery"]

        @DecimalMin("1.0") @DecimalMax("5.0")
        private Double minRating;

        @Min(1) @Max(4)
        private Integer priceLevel;

        private Boolean preferLocal;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class PreferencesResponse {
        private Long id;
        private Integer maxDistanceMeters;
        private Set<String> cuisineTypes;
        private Set<String> businessTypes;
        private Double minRating;
        private Integer priceLevel;
        private Boolean preferLocal;
        private LocalDateTime updatedAt;
    }

    // --- External API response wrappers (internal use) ---

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class GooglePlaceResult {
        private String placeId;
        private String name;
        private String vicinity;  // Address
        private GoogleGeometry geometry;
        private Double rating;
        private Integer userRatingsTotal;
        private Integer priceLevel;
        private List<String> types;
        private GoogleOpeningHours openingHours;
        private List<GooglePhoto> photos;

        @Data @NoArgsConstructor @AllArgsConstructor
        public static class GoogleGeometry {
            private GoogleLocation location;
        }

        @Data @NoArgsConstructor @AllArgsConstructor
        public static class GoogleLocation {
            private Double lat;
            private Double lng;
        }

        @Data @NoArgsConstructor @AllArgsConstructor
        public static class GoogleOpeningHours {
            private Boolean openNow;
        }

        @Data @NoArgsConstructor @AllArgsConstructor
        public static class GooglePhoto {
            private String photoReference;
            private Integer width;
            private Integer height;
        }
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class TripAdvisorLocation {
        private String locationId;
        private String name;
        private Double rating;
        private Integer numReviews;
        private String webUrl;
    }
}
