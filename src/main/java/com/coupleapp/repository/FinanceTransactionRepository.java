package com.coupleapp.repository;

import com.coupleapp.entity.FinanceTransaction;
import com.coupleapp.entity.FinanceTransaction.FinanceCategory;
import com.coupleapp.entity.FinanceTransaction.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface FinanceTransactionRepository extends JpaRepository<FinanceTransaction, Long> {

    // All transactions for a couple in a date range — for monthly reports (paginated)
    @Query("SELECT t FROM FinanceTransaction t " +
           "JOIN FETCH t.recordedBy " +
           "WHERE t.couple.id = :coupleId AND t.transactionDate BETWEEN :start AND :end " +
           "ORDER BY t.transactionDate DESC")
    Page<FinanceTransaction> findByCoupleAndDateRange(
        @Param("coupleId") Long coupleId, 
        @Param("start") LocalDate start, 
        @Param("end") LocalDate end,
        Pageable pageable);

    // Expenses grouped by category — returns [category, total] pairs for the spending breakdown chart
    @Query("SELECT t.category, SUM(t.amount) FROM FinanceTransaction t WHERE t.couple.id = :coupleId AND t.type = 'EXPENSE' AND t.transactionDate BETWEEN :start AND :end GROUP BY t.category ORDER BY SUM(t.amount) DESC")
    List<Object[]> sumExpensesByCategory(@Param("coupleId") Long coupleId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    // Total income or expense for a period — used for balance calculation
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM FinanceTransaction t WHERE t.couple.id = :coupleId AND t.type = :type AND t.transactionDate BETWEEN :start AND :end")
    BigDecimal sumByType(@Param("coupleId") Long coupleId, @Param("type") TransactionType type, @Param("start") LocalDate start, @Param("end") LocalDate end);

    // Recurring transactions — shown in a separate section so the couple can review fixed costs
    List<FinanceTransaction> findByCoupleIdAndIsRecurringTrue(Long coupleId);
}
