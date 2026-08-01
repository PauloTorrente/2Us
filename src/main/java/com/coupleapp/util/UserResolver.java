package com.coupleapp.util;

import com.coupleapp.entity.User;
import com.coupleapp.exception.ForbiddenException;
import com.coupleapp.exception.NotFoundException;
import com.coupleapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

// Converts the authenticated UserDetails (email only) into a full User entity.
// Centralizes this so controllers don't duplicate the lookup.
@Component
@RequiredArgsConstructor
public class UserResolver {

    private final UserRepository userRepository;

    // Resolves the currently authenticated user to a full User entity.
    // Throws 404 if somehow the authenticated user no longer exists in the DB.
    public User resolve(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new NotFoundException("Authenticated user not found in database"));
    }

    // Resolves and also verifies the user belongs to a couple.
    // Use this before any operation that requires couple membership.
    public User resolveWithCouple(UserDetails userDetails) {
        User user = resolve(userDetails);
        if (user.getCouple() == null) {
            throw new ForbiddenException("User is not part of a couple yet. Create or join a couple first.");
        }
        return user;
    }

    // Resolves and verifies the user belongs to a specific couple.
    // Prevents users from accessing other couples' data.
    public User resolveAndVerifyCouple(UserDetails userDetails, Long coupleId) {
        User user = resolveWithCouple(userDetails);
        if (!user.getCouple().getId().equals(coupleId)) {
            throw new ForbiddenException("Access denied: you do not belong to this couple");
        }
        return user;
    }
}
