package com.maquigest.platform.iam.interfaces.acl;

/**
 * Public IAM contract used by other bounded contexts when a subscriber's
 * eligibility must be checked. No IAM domain or persistence types leak.
 */
public interface IamSubscriberFacade {
    boolean isActiveRentalCompany(Long userId);

    /**
     * Locks the IAM user row using PESSIMISTIC_WRITE and checks eligibility.
     * This operation must join an already active write transaction; the lock
     * is held until that transaction completes.
     */
    boolean lockActiveRentalCompany(Long userId);
}
