package com.wissenup.domain.platform.entity;

public enum SchoolStatus {
    PENDING,           // Created but not fully onboarded
    ONBOARDING,        // In the middle of onboarding workflow
    ACTIVE,            // Fully configured, ready to use
    INACTIVE,          // Temporarily disabled
    TRIAL_EXPIRED,     // Trial plan expired
    SUBSCRIPTION_EXPIRED,  // Paid subscription expired
    SUSPENDED,         // Suspended by super admin
    DELETED            // Soft-deleted
}
