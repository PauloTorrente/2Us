package com.coupleapp.repository;

import com.coupleapp.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    // Items belonging to a specific user within a couple — for "My wishlist" view
    List<WishlistItem> findByUserIdAndCoupleIdOrderByCreatedAtDesc(Long userId, Long coupleId);

    // All wishlist items for a couple (both partners) — for the shared wishlist screen
    List<WishlistItem> findByCoupleIdOrderByUserIdAscCreatedAtDesc(Long coupleId);

    // Unfulfilled items only — used to show what still needs to be gifted
    List<WishlistItem> findByCoupleIdAndIsFulfilledFalseOrderByPriorityDesc(Long coupleId);
}
