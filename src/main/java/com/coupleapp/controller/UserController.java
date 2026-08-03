package com.coupleapp.controller;

import com.coupleapp.dto.CoupleDTOs.*;
import com.coupleapp.entity.User;
import com.coupleapp.service.impl.CoupleService;
import com.coupleapp.util.UserResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

// Self-service profile endpoints — currently just the accent color each partner picks
// for themselves, used to color-code their events/items across the app.
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Current user's own profile settings")
public class UserController {

    private final CoupleService coupleService;
    private final UserResolver userResolver;

    // PATCH /api/users/me/color — set or change my own accent color
    @PatchMapping("/me/color")
    @Operation(summary = "Update the authenticated user's accent color")
    public ResponseEntity<MemberResponse> updateMyColor(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateThemeColorRequest request) {

        User user = userResolver.resolve(userDetails);
        return ResponseEntity.ok(coupleService.updateMyThemeColor(user, request));
    }

    // PATCH /api/users/me/avatar — set or change my own profile photo (base64 JPEG)
    @PatchMapping("/me/avatar")
    @Operation(summary = "Update the authenticated user's profile photo")
    public ResponseEntity<MemberResponse> updateMyAvatar(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateAvatarRequest request) {

        User user = userResolver.resolve(userDetails);
        return ResponseEntity.ok(coupleService.updateMyAvatar(user, request));
    }
}
