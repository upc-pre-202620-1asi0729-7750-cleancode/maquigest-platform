package com.maquigest.platform.iam.interfaces.acl;

import java.util.Optional;

/**
 * Published IAM contract for authenticated-account status and roles.
 * Business contexts receive only an immutable snapshot, never an IAM aggregate
 * or persistence entity. Consumers decide which role can perform which action.
 */
public interface IamActorFacade {
    record ActorSnapshot(Long userId, boolean rentalCompany,
                         boolean constructionCompany, boolean administrator) { }

    /** Finds an active account by its authenticated principal email. */
    Optional<ActorSnapshot> findActiveByEmail(String email);
}
