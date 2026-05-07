package com.coupleapp.controller;

import com.coupleapp.dto.CoupleDTOs.*;
import com.coupleapp.entity.User;
import com.coupleapp.service.impl.CoupleService;
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

// Manages the couple lifecycle: creating one, sharing the invite code, joining via code.
// This is the entry gate — the user must have a couple before accessing any other feature.
@RestController
@RequestMapping("/api/couples")
@RequiredArgsConstructor
@Tag(name = "Couples", description = "Create and join couples")
public class CoupleController {

    private final CoupleService coupleService;
    private final UserResolver userResolver;

    // POST /api/couples — first partner creates a new couple
    // Returns the invite code to share with the second partner
    @PostMapping
    @Operation(summary = "Create a new couple (first partner)")
    public ResponseEntity<CoupleResponse> createCouple(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CreateCoupleRequest request) {

        User user = userResolver.resolve(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(coupleService.createCouple(user, request));
    }

    // POST /api/couples/join — second partner joins using the shared invite code
    @PostMapping("/join")
    @Operation(summary = "Join an existing couple using an invite code")
    public ResponseEntity<CoupleResponse> joinCouple(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody JoinCoupleRequest request) {

        User user = userResolver.resolve(userDetails);
        return ResponseEntity.ok(coupleService.joinCouple(user, request));
    }

    // GET /api/couples/me — returns the authenticated user's couple info
    @GetMapping("/me")
    @Operation(summary = "Get the current user's couple details")
    public ResponseEntity<CoupleResponse> getMyCouple(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userResolver.resolve(userDetails);
        return ResponseEntity.ok(coupleService.getMyCouple(user));
    }
}
