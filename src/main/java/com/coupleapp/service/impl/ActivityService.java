package com.coupleapp.service.impl;

import com.coupleapp.dto.ActivityDTOs.*;
import com.coupleapp.entity.*;
import com.coupleapp.exception.ForbiddenException;
import com.coupleapp.exception.NotFoundException;
import com.coupleapp.repository.ActivityLogRepository;
import com.coupleapp.repository.SharedActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final SharedActivityRepository activityRepository;
    private final ActivityLogRepository logRepository;

    public List<ActivityResponse> getActivities(User user) {
        return activityRepository.findByCoupleIdAndIsActiveTrueOrderByCompletionCountDesc(user.getCouple().getId())
                .stream().map(this::mapToResponse).toList();
    }

    @Transactional
    public ActivityResponse createActivity(User user, CreateActivityRequest request) {
        SharedActivity activity = SharedActivity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .couple(user.getCouple())
                .frequency(request.getFrequency())
                .category(request.getCategory())
                .build();

        return mapToResponse(activityRepository.save(activity));
    }

    // Records a completion. Increments the activity's running total for fast queries.
    @Transactional
    public void logCompletion(User user, Long activityId, LogActivityRequest request) {
        SharedActivity activity = getActivityBelongingToCouple(activityId, user.getCouple().getId());

        LocalDateTime completedAt = (request.getCompletedAt() != null)
                ? request.getCompletedAt()
                : LocalDateTime.now(); // Default to now if not specified

        ActivityLog log = ActivityLog.builder()
                .activity(activity)
                .loggedBy(user)
                .note(request.getNote())
                .completedAt(completedAt)
                .build();

        logRepository.save(log);

        // Keep the running count up to date on the activity itself for fast list ordering
        activity.setCompletionCount(activity.getCompletionCount() + 1);
        activityRepository.save(activity);
    }

    // Returns the activity score dashboard: most done, least done, and total completions.
    // The "period" is the last 30 days by default — frontend can pass custom dates.
    public ActivityScoreResponse getScore(User user, int daysBack) {
        Long coupleId = user.getCouple().getId();
        LocalDateTime start = LocalDate.now().minusDays(daysBack).atStartOfDay();
        LocalDateTime end = LocalDateTime.now();

        // Raw DB result: [activityId, completionCount]
        List<Object[]> rows = logRepository.countCompletionsPerActivity(coupleId, start, end);

        // Load all active activities to fill in zero-completion ones (they don't appear in count query)
        List<SharedActivity> allActivities = activityRepository.findByCoupleIdAndIsActiveTrueOrderByCompletionCountDesc(coupleId);

        // Build a map of activityId -> count from the DB rows
        Map<Long, Long> countMap = rows.stream()
                .collect(Collectors.toMap(r -> (Long) r[0], r -> (Long) r[1]));

        // Build ranked list — activities with no logs get count = 0
        List<ActivityRank> ranked = allActivities.stream()
                .map(a -> new ActivityRank(a.getId(), a.getName(), a.getCategory(), countMap.getOrDefault(a.getId(), 0L)))
                .sorted(Comparator.comparingLong(ActivityRank::getCompletionCount).reversed())
                .toList();

        int totalCompletions = ranked.stream().mapToInt(r -> r.getCompletionCount().intValue()).sum();

        // Top 3 most done and bottom 3 least done for the dashboard
        List<ActivityRank> mostDone = ranked.stream().limit(3).toList();
        List<ActivityRank> leastDone = ranked.stream()
                .sorted(Comparator.comparingLong(ActivityRank::getCompletionCount))
                .limit(3).toList();

        return new ActivityScoreResponse(mostDone, leastDone, totalCompletions);
    }

    private SharedActivity getActivityBelongingToCouple(Long activityId, Long coupleId) {
        SharedActivity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new NotFoundException("Activity not found: " + activityId));
        if (!activity.getCouple().getId().equals(coupleId)) {
            throw new ForbiddenException("This activity does not belong to your couple");
        }
        return activity;
    }

    private ActivityResponse mapToResponse(SharedActivity a) {
        return new ActivityResponse(a.getId(), a.getName(), a.getDescription(),
                a.getFrequency(), a.getCategory(), a.getIsActive(), a.getCompletionCount(), a.getCreatedAt());
    }
}
