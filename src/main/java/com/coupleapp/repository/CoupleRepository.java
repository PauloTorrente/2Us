package com.coupleapp.repository;

import com.coupleapp.entity.Couple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoupleRepository extends JpaRepository<Couple, Long> {
    Optional<Couple> findByInviteCode(String inviteCode);
    boolean existsByInviteCode(String inviteCode);
}
