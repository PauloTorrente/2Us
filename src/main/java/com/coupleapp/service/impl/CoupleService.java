package com.coupleapp.service.impl;

import com.coupleapp.dto.CoupleDTOs.*;
import com.coupleapp.entity.Couple;
import com.coupleapp.entity.CoupleStatus;
import com.coupleapp.entity.User;
import com.coupleapp.exception.ConflictException;
import com.coupleapp.exception.NotFoundException;
import com.coupleapp.repository.CoupleRepository;
import com.coupleapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

// Manages the couple lifecycle: creation, invite code generation, and partner joining.
// Everything in the app depends on a couple existing — this is the entry gate.
@Service
@RequiredArgsConstructor
public class CoupleService {

    private final CoupleRepository coupleRepository;
    private final UserRepository userRepository;

    // @Lazy breaks the circular dependency: CoupleService -> TaskService -> (nothing back)
    // Without @Lazy, Spring would try to create both beans simultaneously and fail.
    @Lazy
    private final TaskService taskService;

    // First partner creates a couple. Generates a unique invite code for the second partner.
    @Transactional
    public CoupleResponse createCouple(User creator, CreateCoupleRequest request) {
        if (creator.getCouple() != null) {
            throw new ConflictException("You are already part of a couple");
        }

        Couple couple = Couple.builder()
                .coupleName(request.getCoupleName())
                .relationshipStartDate(request.getRelationshipStartDate())
                .exactDateKnown(request.getExactDateKnown() != null ? request.getExactDateKnown() : true)
                .inviteCode(generateUniqueInviteCode())
                .status(CoupleStatus.PENDING_INVITE)
                .build();

        coupleRepository.save(couple);

        // Link the creator to the couple immediately
        creator.setCouple(couple);
        userRepository.save(creator);

        return mapToResponse(couple);
    }

    // Second partner joins using the invite code shared by the first partner.
    // This is the activation moment — the couple becomes ACTIVE and default tasks are seeded.
    @Transactional
    public CoupleResponse joinCouple(User joiner, JoinCoupleRequest request) {
        if (joiner.getCouple() != null) {
            throw new ConflictException("You are already part of a couple");
        }

        Couple couple = coupleRepository.findByInviteCode(request.getInviteCode())
                .orElseThrow(() -> new NotFoundException("Invalid invite code: " + request.getInviteCode()));

        if (couple.getStatus() != CoupleStatus.PENDING_INVITE) {
            throw new ConflictException("This couple already has two members");
        }

        // Link the second partner
        joiner.setCouple(couple);
        userRepository.save(joiner);

        // Activate the couple
        couple.setStatus(CoupleStatus.ACTIVE);
        // Invalidate the invite code — prevents a third person from joining with the same code
        couple.setInviteCode(null);
        coupleRepository.save(couple);

        // Seed default domestic tasks now that both partners are present.
        // This gives the couple a ready-to-use task list on their first login.
        taskService.seedDefaultTasks(couple);

        return mapToResponse(couple);
    }

    // Returns the current user's couple details
    public CoupleResponse getMyCouple(User user) {
        if (user.getCouple() == null) {
            throw new NotFoundException("You are not part of a couple yet");
        }
        return mapToResponse(user.getCouple());
    }

    // Generates a random 6-character alphanumeric invite code.
    // Retries on collision — statistically this almost never happens but it's handled correctly.
    private String generateUniqueInviteCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        String code;

        do {
            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 6; i++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }
            code = sb.toString();
        } while (coupleRepository.existsByInviteCode(code));

        return code;
    }

    private CoupleResponse mapToResponse(Couple couple) {
        return new CoupleResponse(
                couple.getId(),
                couple.getCoupleName(),
                couple.getRelationshipStartDate(),
                couple.getExactDateKnown(),
                couple.getStatus(),
                couple.getInviteCode(),
                couple.getCreatedAt()
        );
    }
}
