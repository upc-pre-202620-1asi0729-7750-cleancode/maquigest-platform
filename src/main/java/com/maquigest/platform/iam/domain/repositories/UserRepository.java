package com.maquigest.platform.iam.domain.repositories;

import com.maquigest.platform.iam.domain.model.aggregates.User;

/** Persistence port for the IAM user aggregate. */
public interface UserRepository {
    User save(User user);

    boolean existsByEmail(String email);
}
