package com.maquigest.platform.iam.domain.repositories;

import com.maquigest.platform.iam.domain.model.aggregates.User;

import java.util.Optional;

/** Persistence port for the IAM user aggregate. */
public interface UserRepository {
    Optional<User> findByEmail(String email);

    User save(User user);

    boolean existsByEmail(String email);
}
