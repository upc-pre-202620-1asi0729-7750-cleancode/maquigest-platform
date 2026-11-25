package com.maquigest.platform.iam.domain.model.valueobjects;

/** Identifies the referenced rental or rental request within Rentals. */
public record NotificationReferenceId(Long referenceId) {
    public NotificationReferenceId {
        if (referenceId == null || referenceId < 1) {
            throw new IllegalArgumentException("validation.notification-reference-id.required");
        }
    }
}
