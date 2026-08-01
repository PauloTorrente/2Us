package com.coupleapp.entity;

// Lifecycle state of a couple. Drives which features are accessible at each stage.
public enum CoupleStatus {

    // Creator set up the couple, waiting for the invite code to be used.
    PENDING_INVITE,

    // Default state before a couple is explicitly initialized.
    PENDING,

    // Both partners are in — full feature access.
    ACTIVE,

    // Temporarily paused (e.g. break). Data is kept but access is restricted.
    PAUSED,

    // Relationship ended. Data is archived and read-only.
    ENDED;

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean canModify() {
        return this == ACTIVE || this == PENDING || this == PENDING_INVITE;
    }

    public boolean isPending() {
        return this == PENDING || this == PENDING_INVITE;
    }
}
