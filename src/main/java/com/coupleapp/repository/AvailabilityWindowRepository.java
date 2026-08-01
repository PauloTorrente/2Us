package com.coupleapp.repository;

import com.coupleapp.entity.AvailabilityWindow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvailabilityWindowRepository extends JpaRepository<AvailabilityWindow, Long> {
    List<AvailabilityWindow> findByCoupleIdOrderByStartDateAsc(Long coupleId);
    List<AvailabilityWindow> findByCoupleIdAndUserId(Long coupleId, Long userId);
}
