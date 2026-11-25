package com.maquigest.platform.iam.application.internal.commandservices;
import com.maquigest.platform.iam.domain.model.valueobjects.RecoveryTokenHash;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;
import java.util.HexFormat;
final class PasswordRecoveryTokens {
    private static final SecureRandom RANDOM = new SecureRandom();
    private PasswordRecoveryTokens() { }
    static String generate() {
        byte[] bytes = new byte[32]; RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    static RecoveryTokenHash hash(String token) {
        try {
            return new RecoveryTokenHash(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8))));
        } catch (NoSuchAlgorithmException e) { throw new IllegalStateException("SHA-256 unavailable"); }
    }
    static boolean valid(String token) { return token != null && token.matches("[A-Za-z0-9_-]{43}"); }
}
