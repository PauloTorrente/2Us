package com.coupleapp.service.impl;

import com.coupleapp.dto.WishlistDTOs.*;
import com.coupleapp.entity.WishlistItem;
import com.coupleapp.entity.User;
import com.coupleapp.exception.ForbiddenException;
import com.coupleapp.exception.NotFoundException;
import com.coupleapp.repository.WishlistItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistItemRepository wishlistItemRepository;

    // Returns both partners' wishlists combined — for the shared view
    public List<WishlistItemResponse> getCoupleWishlist(User user) {
        return wishlistItemRepository.findByCoupleIdOrderByUserIdAscCreatedAtDesc(user.getCouple().getId())
                .stream().map(this::mapToResponse).toList();
    }

    // Returns only the requesting user's wishlist items
    public List<WishlistItemResponse> getMyWishlist(User user) {
        return wishlistItemRepository.findByUserIdAndCoupleIdOrderByCreatedAtDesc(user.getId(), user.getCouple().getId())
                .stream().map(this::mapToResponse).toList();
    }

    @Transactional
    public WishlistItemResponse addItem(User user, CreateWishlistItemRequest request) {
        WishlistItem item = WishlistItem.builder()
                .title(request.getTitle())
                .note(request.getNote())
                .productUrl(request.getProductUrl())
                .imageUrl(request.getImageUrl())
                .price(request.getPrice())
                .source(request.getSource() != null ? request.getSource() : WishlistItem.WishlistSource.MANUAL)
                .priority(request.getPriority() != null ? request.getPriority() : WishlistItem.Priority.MEDIUM)
                .user(user)
                .couple(user.getCouple())
                .build();

        return mapToResponse(wishlistItemRepository.save(item));
    }

    // Partner marks an item as gifted/purchased
    @Transactional
    public WishlistItemResponse fulfillItem(User user, Long itemId) {
        WishlistItem item = getItemBelongingToCouple(itemId, user.getCouple().getId());
        item.setIsFulfilled(true);
        return mapToResponse(wishlistItemRepository.save(item));
    }

    @Transactional
    public void deleteItem(User user, Long itemId) {
        WishlistItem item = getItemBelongingToCouple(itemId, user.getCouple().getId());
        // Only the owner can delete their own wishlist item
        if (!item.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You can only delete your own wishlist items");
        }
        wishlistItemRepository.delete(item);
    }

    private WishlistItem getItemBelongingToCouple(Long itemId, Long coupleId) {
        WishlistItem item = wishlistItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Wishlist item not found: " + itemId));
        if (!item.getCouple().getId().equals(coupleId)) {
            throw new ForbiddenException("This item does not belong to your couple");
        }
        return item;
    }

    private WishlistItemResponse mapToResponse(WishlistItem item) {
        return new WishlistItemResponse(item.getId(), item.getTitle(), item.getNote(),
                item.getProductUrl(), item.getImageUrl(), item.getPrice(),
                item.getSource(), item.getPriority(), item.getIsFulfilled(),
                item.getUser().getId(), item.getUser().getName(), item.getCreatedAt());
    }
}
