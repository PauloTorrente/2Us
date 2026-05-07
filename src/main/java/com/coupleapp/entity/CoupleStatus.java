package com.coupleapp.entity;

/**
 * Status of a couple relationship in the system.
 * Controls access and features based on relationship state.
 */
public enum CoupleStatus {
    
    /**
     * One partner created the couple, waiting for second partner to accept.
     */
    PENDING_INVITE,
    
    /**
     * Invite was sent, waiting for acceptance.
     */
    PENDING,
    
    /**
     * Both partners accepted, couple is active and can use all features.
     */
    ACTIVE,
    
    /**
     * Couple is temporarily paused (e.g., break, separation).
     * Data is preserved but access is restricted.
     */
    PAUSED,
    
    /**
     * Couple ended the relationship.
     * Data is archived, no further changes allowed.
     */
    ENDED;
    
    /**
     * Check if this status allows full couple features.
     */
    public boolean isActive() {
        return this == ACTIVE;
    }
    
    /**
     * Check if this status allows data modifications.
     */
    public boolean canModify() {
        return this == ACTIVE || this == PENDING || this == PENDING_INVITE;
    }
    
    /**
     * Check if waiting for partner acceptance.
     */
    public boolean isPending() {
        return this == PENDING || this == PENDING_INVITE;
    }
}
