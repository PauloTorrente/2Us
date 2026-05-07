package com.coupleapp.controller;

import com.coupleapp.dto.SuggestionDTOs.PlaceSuggestionRequest;
import com.coupleapp.dto.SuggestionDTOs.PlaceSuggestionResponse;
import com.coupleapp.entity.User;
import com.coupleapp.service.impl.SuggestionService;
import com.coupleapp.util.UserResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/suggestions")
@RequiredArgsConstructor
@Tag(name = "Suggestions", description = "Place and activity suggestions based on couple preferences")
public class SuggestionController {

    private final SuggestionService suggestionService;
    private final UserResolver userResolver;

    @PostMapping
    @Operation(summary = "Get place suggestions (restaurants, cafes, etc.) based on location and preferences")
    public ResponseEntity<List<PlaceSuggestionResponse>> getSuggestions(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PlaceSuggestionRequest request) {

        try {
            User user = userResolver.resolveWithCouple(userDetails);
            log.info("Getting suggestions for user: {}, request: {}", user.getId(), request);

            List<PlaceSuggestionResponse> suggestions = suggestionService.getSuggestions(user, request);
            return ResponseEntity.ok(suggestions);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid suggestion request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error getting suggestions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/history")
    @Operation(summary = "Get suggestion history (not yet implemented)")
    public ResponseEntity<?> getSuggestionHistory(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userResolver.resolveWithCouple(userDetails);
            log.info("Getting suggestion history for user: {}", user.getId());
            // TODO: Implement suggestion history retrieval
            return ResponseEntity.ok("Suggestion history not yet implemented");
        } catch (Exception e) {
            log.error("Error getting suggestion history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving suggestion history");
        }
    }
}
