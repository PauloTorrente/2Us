package com.coupleapp.service.impl;

import com.coupleapp.dto.FinanceDTOs.*;
import com.coupleapp.entity.*;
import com.coupleapp.entity.FinanceGoal.GoalStatus;
import com.coupleapp.entity.FinanceTransaction.TransactionType;
import com.coupleapp.exception.ForbiddenException;
import com.coupleapp.exception.NotFoundException;
import com.coupleapp.repository.FinanceGoalRepository;
import com.coupleapp.repository.FinanceTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FinanceService {

    private final FinanceTransactionRepository transactionRepository;
    private final FinanceGoalRepository goalRepository;

    // --- Transactions ---

    public Page<TransactionResponse> getTransactions(User user, LocalDate start, LocalDate end, Pageable pageable) {
        return transactionRepository.findByCoupleAndDateRange(user.getCouple().getId(), start, end, pageable)
                .map(this::mapTransactionToResponse);
    }

    @Transactional
    public TransactionResponse createTransaction(User user, CreateTransactionRequest request) {
        FinanceTransaction tx = FinanceTransaction.builder()
                .description(request.getDescription())
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory())
                .couple(user.getCouple())
                .recordedBy(user)
                .transactionDate(request.getTransactionDate())
                .note(request.getNote())
                .isRecurring(request.getIsRecurring() != null ? request.getIsRecurring() : false)
                .build();

        return mapTransactionToResponse(transactionRepository.save(tx));
    }

    // Returns income, expenses, balance, and per-category breakdown for a period.
    // The category breakdown is what the frontend uses to show spending tips.
    public FinanceSummaryResponse getSummary(User user, LocalDate start, LocalDate end) {
        Long coupleId = user.getCouple().getId();

        BigDecimal totalIncome = transactionRepository.sumByType(coupleId, TransactionType.INCOME, start, end);
        BigDecimal totalExpenses = transactionRepository.sumByType(coupleId, TransactionType.EXPENSE, start, end);
        BigDecimal balance = totalIncome.subtract(totalExpenses);

        // Raw rows from DB: [category_name, sum]
        List<Object[]> categoryRows = transactionRepository.sumExpensesByCategory(coupleId, start, end);

        // Build an ordered map (highest spending first) for the chart
        Map<String, BigDecimal> expensesByCategory = new LinkedHashMap<>();
        String topCategory = null;

        for (Object[] row : categoryRows) {
            String categoryName = row[0].toString();
            BigDecimal total = (BigDecimal) row[1];
            expensesByCategory.put(categoryName, total);
            if (topCategory == null) topCategory = categoryName; // First row = highest
        }

        return new FinanceSummaryResponse(totalIncome, totalExpenses, balance, expensesByCategory, topCategory);
    }

    // --- Goals ---

    public List<GoalResponse> getGoals(User user) {
        return goalRepository.findByCoupleIdOrderByCreatedAtDesc(user.getCouple().getId())
                .stream().map(this::mapGoalToResponse).toList();
    }

    @Transactional
    public GoalResponse createGoal(User user, CreateGoalRequest request) {
        FinanceGoal goal = FinanceGoal.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .targetAmount(request.getTargetAmount())
                .currentAmount(BigDecimal.ZERO)
                .targetDate(request.getTargetDate())
                .horizon(request.getHorizon())
                .couple(user.getCouple())
                .build();

        return mapGoalToResponse(goalRepository.save(goal));
    }

    // Adds a contribution to a goal. Automatically marks it ACHIEVED when target is reached.
    @Transactional
    public GoalResponse contributeToGoal(User user, Long goalId, GoalContributionRequest request) {
        FinanceGoal goal = getGoalBelongingToCouple(goalId, user.getCouple().getId());

        goal.setCurrentAmount(goal.getCurrentAmount().add(request.getAmount()));

        // Auto-complete the goal when the target is reached
        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(GoalStatus.ACHIEVED);
        }

        return mapGoalToResponse(goalRepository.save(goal));
    }

    private FinanceGoal getGoalBelongingToCouple(Long goalId, Long coupleId) {
        FinanceGoal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new NotFoundException("Goal not found: " + goalId));
        if (!goal.getCouple().getId().equals(coupleId)) {
            throw new ForbiddenException("This goal does not belong to your couple");
        }
        return goal;
    }

    private TransactionResponse mapTransactionToResponse(FinanceTransaction tx) {
        return new TransactionResponse(
                tx.getId(), tx.getDescription(), tx.getAmount(), tx.getType(),
                tx.getCategory(), tx.getTransactionDate(), tx.getNote(),
                tx.getIsRecurring(), tx.getRecordedBy().getName(), tx.getCreatedAt()
        );
    }

    private GoalResponse mapGoalToResponse(FinanceGoal goal) {
        // Calculate progress percentage for the UI progress bar
        int percent = goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0
                ? goal.getCurrentAmount().multiply(BigDecimal.valueOf(100))
                .divide(goal.getTargetAmount(), 0, RoundingMode.HALF_UP).intValue()
                : 0;

        return new GoalResponse(
                goal.getId(), goal.getTitle(), goal.getDescription(),
                goal.getTargetAmount(), goal.getCurrentAmount(),
                Math.min(percent, 100), // Cap at 100% even if over-contributed
                goal.getTargetDate(), goal.getHorizon(), goal.getStatus(), goal.getCreatedAt()
        );
    }
}
