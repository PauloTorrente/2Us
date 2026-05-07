package com.coupleapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// A single item on a partner's wishlist.
// Can be a product link (Shopee, Amazon, Steam), an image URL, or just plain text.
@Entity
@Table(name = "wishlist_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // What the person wants (e.g. "AirPods Pro", "Zelda: Tears of the Kingdom")
    @Column(nullable = false, length = 200)
    private String title;

    // Optional note (e.g. "The black color, size M")
    @Column(length = 500)
    private String note;

    // URL to the product page on Shopee, Amazon, Steam, etc.
    @Column(name = "product_url", length = 500)
    private String productUrl;

    // URL to an image of the item (either from the product page or uploaded directly)
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    // Approximate price — helps the partner know what they're getting into
    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    // Where this item comes from — for display icon in the frontend
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private WishlistSource source = WishlistSource.MANUAL;

    // Priority level this person assigns to the item
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    // Whether the item has been gifted/purchased — the partner marks this
    @Column(name = "is_fulfilled")
    @Builder.Default
    private Boolean isFulfilled = false;

    // The user whose wishlist this item belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // The couple context — allows fetching both partners' wishlists together
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couple_id", nullable = false)
    private Couple couple;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum WishlistSource {
        MANUAL,  // User typed it directly
        LINK,    // URL to a product page (Shopee, Amazon, Steam, etc.)
        IMAGE    // User uploaded or linked an image
    }

    public enum Priority {
        LOW,
        MEDIUM,
        HIGH
    }
}
