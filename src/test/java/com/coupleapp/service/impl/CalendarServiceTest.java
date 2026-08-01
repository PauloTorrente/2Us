package com.coupleapp.service.impl;

import com.coupleapp.dto.CalendarDTOs.AvailabilityOverlapResponse;
import com.coupleapp.dto.CalendarDTOs.CreateAvailabilityWindowRequest;
import com.coupleapp.entity.AvailabilityWindow;
import com.coupleapp.entity.AvailabilityWindow.AvailabilityType;
import com.coupleapp.entity.Couple;
import com.coupleapp.entity.User;
import com.coupleapp.exception.ForbiddenException;
import com.coupleapp.repository.AvailabilityWindowRepository;
import com.coupleapp.repository.CalendarEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalendarServiceTest {

    @Mock private CalendarEventRepository eventRepository;
    @Mock private AvailabilityWindowRepository availabilityRepository;

    private CalendarService calendarService;

    private Couple couple;
    private User partnerA;
    private User partnerB;

    @BeforeEach
    void setUp() {
        calendarService = new CalendarService(eventRepository, availabilityRepository);
        couple = Couple.builder().id(1L).build();
        partnerA = User.builder().id(10L).name("Ana").couple(couple).build();
        partnerB = User.builder().id(20L).name("Bruno").couple(couple).build();
    }

    private AvailabilityWindow window(User owner, String start, String end) {
        return AvailabilityWindow.builder()
                .user(owner).couple(couple)
                .startDate(LocalDate.parse(start)).endDate(LocalDate.parse(end))
                .type(AvailabilityType.VACATION).build();
    }

    @Test
    void getAvailabilityOverlaps_overlappingWindows_returnsIntersection() {
        when(availabilityRepository.findByCoupleIdOrderByStartDateAsc(1L)).thenReturn(List.of(
                window(partnerA, "2026-01-10", "2026-01-20"),
                window(partnerB, "2026-01-15", "2026-01-25")
        ));

        List<AvailabilityOverlapResponse> overlaps = calendarService.getAvailabilityOverlaps(partnerA);

        assertThat(overlaps).hasSize(1);
        assertThat(overlaps.get(0).getStartDate()).isEqualTo(LocalDate.parse("2026-01-15"));
        assertThat(overlaps.get(0).getEndDate()).isEqualTo(LocalDate.parse("2026-01-20"));
    }

    @Test
    void getAvailabilityOverlaps_nonOverlappingWindows_returnsEmpty() {
        when(availabilityRepository.findByCoupleIdOrderByStartDateAsc(1L)).thenReturn(List.of(
                window(partnerA, "2026-01-01", "2026-01-05"),
                window(partnerB, "2026-02-01", "2026-02-05")
        ));

        assertThat(calendarService.getAvailabilityOverlaps(partnerA)).isEmpty();
    }

    @Test
    void getAvailabilityOverlaps_multipleWindowsPerPartner_intersectsEveryPair() {
        when(availabilityRepository.findByCoupleIdOrderByStartDateAsc(1L)).thenReturn(List.of(
                window(partnerA, "2026-01-01", "2026-01-10"),
                window(partnerA, "2026-03-01", "2026-03-10"),
                window(partnerB, "2026-01-05", "2026-01-08"),
                window(partnerB, "2026-03-05", "2026-03-20")
        ));

        List<AvailabilityOverlapResponse> overlaps = calendarService.getAvailabilityOverlaps(partnerA);

        assertThat(overlaps).hasSize(2);
    }

    @Test
    void createAvailabilityWindow_endBeforeStart_throwsForbidden() {
        CreateAvailabilityWindowRequest request = new CreateAvailabilityWindowRequest(
                LocalDate.parse("2026-01-20"), LocalDate.parse("2026-01-10"), AvailabilityType.DAY_OFF, null);

        assertThatThrownBy(() -> calendarService.createAvailabilityWindow(partnerA, request))
                .isInstanceOf(ForbiddenException.class);
    }
}
