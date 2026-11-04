package com.maquigest.platform.iam.infrastructure.hashing.bcrypt.services;

import com.maquigest.platform.iam.infrastructure.hashing.bcrypt.BCryptHashingService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/** One BCrypt implementation serves both the application port and Spring Security. */
@Service
public class HashingServiceImpl implements BCryptHashingService {
    private final BCryptPasswordEncoder passwordEncoder;

    public HashingServiceImpl() {
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public String encode(CharSequence rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
