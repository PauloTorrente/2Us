package com.coupleapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

// Stores preferences for local business and restaurant suggestions.
// Each couple has one preferences record that controls what types of places they want to see.
@Entity
@Table(name = "suggestion_preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuggestionPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The couple these preferences belong to (one-to-one relationship)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couple_id", nullable = false, unique = true)
    private Couple couple;

    // Maximum distance in meters for suggestions (default: 5000m = 5km)
    @Column(name = "max_distance_meters")
    @Builder.Default
    private Integer maxDistanceMeters = 5000;

    // Preferred cuisine types (e.g., "italian", "japanese", "mexican", "brazilian")
    // Stored as comma-separated values for simplicity
    @Column(name = "cuisine_types", length = 500)
    private String cuisineTypes;

    // Preferred business types (e.g., "cafe", "bakery", "restaurant", "bar")
    @Column(name = "business_types", length = 500)
    private String businessTypes;

    // Minimum rating threshold (1-5). Only show places with rating >= this value.
    @Column(name = "min_rating")
    @Builder.Default
    private Double minRating = 3.5;

    // Price level preference: 1 (cheap), 2 (moderate), 3 (expensive), 4 (very expensive)
    // Null = no preference
    @Column(name = "price_level")
    private Integer priceLevel;

    // Whether to prioritize highly-rated local businesses over chains
    @Column(name = "prefer_local")
    @Builder.Default
    private Boolean preferLocal = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper methods to work with comma-separated strings

    public Set<String> getCuisineTypesSet() {
        if (cuisineTypes == null || cuisineTypes.isBlank()) {
            return new HashSet<>();
        }
        return Set.of(cuisineTypes.split(","));
    }

    public void setCuisineTypesFromSet(Set<String> types) {
        this.cuisineTypes = types == null || types.isEmpty() 
            ? null 
            : String.join(",", types);
    }

    public Set<String> getBusinessTypesSet() {
        if (businessTypes == null || businessTypes.isBlank()) {
            return new HashSet<>();
        }
        return Set.of(businessTypes.split(","));
    }

    public void setBusinessTypesFromSet(Set<String> types) {
        this.businessTypes = types == null || types.isEmpty() 
            ? null 
            : String.join(",", types);
    }
}
