package com.maquigest.platform.iam.application.internal.outboundservices.hashing;

/** Outbound port for password hashing operations in IAM. */
public interface HashingService {
    String encode(CharSequence rawPassword);

    boolean matches(CharSequence rawPassword, String encodedPassword);
}
