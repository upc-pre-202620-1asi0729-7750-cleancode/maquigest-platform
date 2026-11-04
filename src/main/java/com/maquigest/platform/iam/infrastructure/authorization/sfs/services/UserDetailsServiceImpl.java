package com.maquigest.platform.iam.infrastructure.authorization.sfs.services;

import com.maquigest.platform.iam.domain.model.valueobjects.UserStatus;
import com.maquigest.platform.iam.domain.repositories.UserRepository;
import com.maquigest.platform.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** Reloads the account from MySQL for every authenticated request. */
@Service("defaultUserDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Account not found"));
        // Existing JWTs must not grant access to newly disabled accounts.
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UsernameNotFoundException("Account not active");
        }
        return UserDetailsImpl.build(user);
    }
}
