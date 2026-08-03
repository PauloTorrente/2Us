package com.coupleapp.dto;

import com.coupleapp.entity.FinanceTransaction.*;
import com.coupleapp.entity.FinanceGoal.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class FinanceDTOs {

    // --- Transactions ---

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class CreateTransactionRequest {
        @NotBlank(message = "Description is required")
        @Size(max = 150)
        private String description;

        @NotNull @DecimalMin("0.01")
        private BigDecimal amount;

        @NotNull private TransactionType type;
        @NotNull private FinanceCategory category;

        @NotNull private LocalDate transactionDate;

        @Size(max = 300)
        private String note;

        private Boolean isRecurring = false;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class TransactionResponse {
        private Long id;
        private String description;
        private BigDecimal amount;
        private TransactionType type;
        private FinanceCategory category;
        private LocalDate transactionDate;
        private String note;
        private Boolean isRecurring;
        private String recordedByName;
        private LocalDateTime createdAt;
    }

    // --- Summary (used for dashboard) ---

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class FinanceSummaryResponse {
        private BigDecimal totalIncome;
        private BigDecimal totalExpenses;
        private BigDecimal balance;
        private Map<String, BigDecimal> expensesByCategory; // Category name -> total
        private String topSpendingCategory; // Highest spending category — front uses this for tips
    }

    // --- Insights (recurring-purchase reminders + next-month spending forecast) ---

    // One group of recurring transactions (same description+category, isRecurring=true),
    // e.g. "Mercado" bought a few times — lets the app suggest "buy again" with a realistic
    // expected amount.
    @Data @NoArgsConstructor @AllArgsConstructor
    public static class RecurringReminder {
        private String description;
        private FinanceCategory category;
        private BigDecimal averageAmount;
        private BigDecimal lastAmount;
        private LocalDate lastPurchasedDate;
        private int occurrenceCount;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class FinanceInsightsResponse {
        private List<RecurringReminder> recurringReminders;
        // Simple average of total expenses over the last `forecastBasisMonths` full months —
        // a heuristic, not a prediction model. Deliberately not fancier than that.
        private BigDecimal nextMonthForecast;
        private int forecastBasisMonths;
    }

    // --- Goals ---

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class CreateGoalRequest {
        @NotBlank @Size(max = 150)
        private String title;

        @Size(max = 500)
        private String description;

        @NotNull @DecimalMin("1.00")
        private BigDecimal targetAmount;

        private LocalDate targetDate;

        @NotNull
        private GoalHorizon horizon;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class GoalContributionRequest {
        @NotNull @DecimalMin("0.01")
        private BigDecimal amount;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class GoalResponse {
        private Long id;
        private String title;
        private String description;
        private BigDecimal targetAmount;
        private BigDecimal currentAmount;
        private Integer progressPercent; // 0-100 — used for the progress bar in the UI
        private LocalDate targetDate;
        private GoalHorizon horizon;
        private GoalStatus status;
        private LocalDateTime createdAt;
    }
}
