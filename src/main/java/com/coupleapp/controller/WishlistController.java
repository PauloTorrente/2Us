package com.coupleapp.controller;

import com.coupleapp.dto.WishlistDTOs.*;
import com.coupleapp.entity.User;
import com.coupleapp.service.impl.LinkUnfurlService;
import com.coupleapp.service.impl.WishlistService;
import com.coupleapp.util.UserResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Wishlist management: add items via link, image, or text. Partner marks as fulfilled.
@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@Tag(name = "Wishlist", description = "Each partner's wishlist — links, images, or text items")
public class WishlistController {

    private final WishlistService wishlistService;
    private final UserResolver userResolver;
    private final LinkUnfurlService linkUnfurlService;

    // GET /api/wishlist — both partners' wishlists combined
    @GetMapping
    @Operation(summary = "Get the couple's combined wishlist")
    public ResponseEntity<List<WishlistItemResponse>> getCoupleWishlist(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userResolver.resolveWithCouple(userDetails);
        return ResponseEntity.ok(wishlistService.getCoupleWishlist(user));
    }

    // GET /api/wishlist/mine — only the requesting user's items
    @GetMapping("/mine")
    @Operation(summary = "Get my own wishlist items")
    public ResponseEntity<List<WishlistItemResponse>> getMyWishlist(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userResolver.resolveWithCouple(userDetails);
        return ResponseEntity.ok(wishlistService.getMyWishlist(user));
    }

    // POST /api/wishlist — add an item (Shopee link, Amazon, Steam, image URL, or plain text)
    @PostMapping
    @Operation(summary = "Add an item to my wishlist")
    public ResponseEntity<WishlistItemResponse> addItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateWishlistItemRequest request) {

        User user = userResolver.resolveWithCouple(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(wishlistService.addItem(user, request));
    }

    // PATCH /api/wishlist/{itemId}/fulfill — partner marks this as gifted/purchased
    @PatchMapping("/{itemId}/fulfill")
    @Operation(summary = "Mark a wishlist item as fulfilled (gifted)")
    public ResponseEntity<WishlistItemResponse> fulfillItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId) {

        User user = userResolver.resolveWithCouple(userDetails);
        return ResponseEntity.ok(wishlistService.fulfillItem(user, itemId));
    }

    // POST /api/wishlist/unfurl — best-effort preview (title/image/price) from a pasted link.
    // Doesn't persist anything, just fills the "add item" form. Never fails hard — fields come
    // back null when the page couldn't be parsed.
    @PostMapping("/unfurl")
    @Operation(summary = "Preview title/image/price from a product link")
    public ResponseEntity<UnfurlResponse> unfurl(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UnfurlRequest request) {

        userResolver.resolveWithCouple(userDetails);
        var result = linkUnfurlService.unfurl(request.getUrl());
        return ResponseEntity.ok(new UnfurlResponse(result.title(), result.imageUrl(), result.price()));
    }

    // DELETE /api/wishlist/{itemId} — only the owner can delete their own item
    @DeleteMapping("/{itemId}")
    @Operation(summary = "Delete a wishlist item (owner only)")
    public ResponseEntity<Void> deleteItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId) {

        User user = userResolver.resolveWithCouple(userDetails);
        wishlistService.deleteItem(user, itemId);
        return ResponseEntity.noContent().build();
    }
}
