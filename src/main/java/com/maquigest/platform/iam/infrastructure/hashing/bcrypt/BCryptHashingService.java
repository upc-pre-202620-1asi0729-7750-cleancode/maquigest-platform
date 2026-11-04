package com.maquigest.platform.iam.infrastructure.hashing.bcrypt;

import com.maquigest.platform.iam.application.internal.outboundservices.hashing.HashingService;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Bridges the IAM hashing port and Spring Security's PasswordEncoder contract. */
public interface BCryptHashingService extends HashingService, PasswordEncoder {
}
