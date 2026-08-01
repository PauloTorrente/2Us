package com.coupleapp.service.impl;

import com.coupleapp.dto.FinanceDTOs.GoalContributionRequest;
import com.coupleapp.dto.FinanceDTOs.GoalResponse;
import com.coupleapp.entity.Couple;
import com.coupleapp.entity.FinanceGoal;
import com.coupleapp.entity.FinanceGoal.GoalHorizon;
import com.coupleapp.entity.FinanceGoal.GoalStatus;
import com.coupleapp.entity.User;
import com.coupleapp.exception.ForbiddenException;
import com.coupleapp.exception.NotFoundException;
import com.coupleapp.repository.FinanceGoalRepository;
import com.coupleapp.repository.FinanceTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinanceServiceTest {

    @Mock private FinanceTransactionRepository transactionRepository;
    @Mock private FinanceGoalRepository goalRepository;

    private FinanceService financeService;

    private Couple couple;
    private User user;

    @BeforeEach
    void setUp() {
        financeService = new FinanceService(transactionRepository, goalRepository);
        couple = Couple.builder().id(1L).build();
        user = User.builder().id(10L).name("Ana").couple(couple).build();
    }

    private FinanceGoal goal(BigDecimal target, BigDecimal current) {
        return FinanceGoal.builder().id(1L).title("Viagem").targetAmount(target).currentAmount(current)
                .horizon(GoalHorizon.SHORT).status(GoalStatus.IN_PROGRESS).couple(couple).build();
    }

    @Test
    void contributeToGoal_reachesTarget_marksAchieved() {
        when(goalRepository.save(any(FinanceGoal.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal(BigDecimal.valueOf(1000), BigDecimal.valueOf(900))));

        GoalResponse response = financeService.contributeToGoal(user, 1L, new GoalContributionRequest(BigDecimal.valueOf(100)));

        assertThat(response.getStatus()).isEqualTo(GoalStatus.ACHIEVED);
        assertThat(response.getProgressPercent()).isEqualTo(100);
    }

    @Test
    void contributeToGoal_belowTarget_staysInProgress() {
        when(goalRepository.save(any(FinanceGoal.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal(BigDecimal.valueOf(1000), BigDecimal.valueOf(200))));

        GoalResponse response = financeService.contributeToGoal(user, 1L, new GoalContributionRequest(BigDecimal.valueOf(100)));

        assertThat(response.getStatus()).isEqualTo(GoalStatus.IN_PROGRESS);
        assertThat(response.getProgressPercent()).isEqualTo(30);
    }

    @Test
    void contributeToGoal_goalNotFound_throwsNotFound() {
        when(goalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> financeService.contributeToGoal(user, 99L, new GoalContributionRequest(BigDecimal.TEN)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void contributeToGoal_goalBelongsToAnotherCouple_throwsForbidden() {
        FinanceGoal othersGoal = goal(BigDecimal.valueOf(1000), BigDecimal.ZERO);
        othersGoal.setCouple(Couple.builder().id(2L).build());
        when(goalRepository.findById(1L)).thenReturn(Optional.of(othersGoal));

        assertThatThrownBy(() -> financeService.contributeToGoal(user, 1L, new GoalContributionRequest(BigDecimal.TEN)))
                .isInstanceOf(ForbiddenException.class);
    }
}
