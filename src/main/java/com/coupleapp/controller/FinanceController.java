package com.coupleapp.controller;

import com.coupleapp.dto.FinanceDTOs.*;
import com.coupleapp.entity.User;
import com.coupleapp.service.impl.FinanceService;
import com.coupleapp.util.UserResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

// Financial management: transactions, monthly summary, and savings goals.
@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
@Tag(name = "Finance", description = "Transactions, spending summary, and savings goals")
public class FinanceController {

    private final FinanceService financeService;
    private final UserResolver userResolver;

    // --- Transactions ---

    // GET /api/finance/transactions?start=2025-01-01&end=2025-01-31&page=0&size=20
    @GetMapping("/transactions")
    @Operation(summary = "Get transactions in a date range (paginated)")
    public ResponseEntity<Page<TransactionResponse>> getTransactions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        User user = userResolver.resolveWithCouple(userDetails);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(financeService.getTransactions(user, start, end, pageable));
    }

    // POST /api/finance/transactions — record a new income or expense
    @PostMapping("/transactions")
    @Operation(summary = "Record a new transaction")
    public ResponseEntity<TransactionResponse> createTransaction(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateTransactionRequest request) {

        User user = userResolver.resolveWithCouple(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(financeService.createTransaction(user, request));
    }

    // GET /api/finance/summary?start=2025-01-01&end=2025-01-31
    // Returns income, expenses, balance, and category breakdown for spending tips
    @GetMapping("/summary")
    @Operation(summary = "Get financial summary with spending breakdown by category")
    public ResponseEntity<FinanceSummaryResponse> getSummary(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        User user = userResolver.resolveWithCouple(userDetails);
        return ResponseEntity.ok(financeService.getSummary(user, start, end));
    }

    // --- Goals ---

    // GET /api/finance/goals — all savings goals (house, car, vacation, etc.)
    @GetMapping("/goals")
    @Operation(summary = "Get all savings goals")
    public ResponseEntity<List<GoalResponse>> getGoals(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userResolver.resolveWithCouple(userDetails);
        return ResponseEntity.ok(financeService.getGoals(user));
    }

    // POST /api/finance/goals — create a new savings goal
    @PostMapping("/goals")
    @Operation(summary = "Create a new savings goal")
    public ResponseEntity<GoalResponse> createGoal(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateGoalRequest request) {

        User user = userResolver.resolveWithCouple(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(financeService.createGoal(user, request));
    }

    // POST /api/finance/goals/{goalId}/contribute — add money toward a goal
    @PostMapping("/goals/{goalId}/contribute")
    @Operation(summary = "Add a contribution to a savings goal")
    public ResponseEntity<GoalResponse> contributeToGoal(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long goalId,
            @Valid @RequestBody GoalContributionRequest request) {

        User user = userResolver.resolveWithCouple(userDetails);
        return ResponseEntity.ok(financeService.contributeToGoal(user, goalId, request));
    }
}
