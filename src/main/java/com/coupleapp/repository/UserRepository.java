package com.coupleapp.repository;

import com.coupleapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    // Both partners of a couple — used for random task assignment and shared notifications
    List<User> findByCoupleId(Long coupleId);
}
