package com.maquigest.platform.iam.application.internal.outboundservices.tokens;

/** Outbound port for IAM bearer token issuance and validation (no JWT dependency here). */
public interface TokenService {
    String generateToken(String email);

    String getUsernameFromToken(String token);

    boolean validateToken(String token);
}
