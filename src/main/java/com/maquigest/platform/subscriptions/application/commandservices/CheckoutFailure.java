package com.maquigest.platform.subscriptions.application.commandservices;
import com.maquigest.platform.shared.application.result.ApplicationError;

public final class CheckoutFailure extends RuntimeException {
    public enum Code { UNAVAILABLE, FORBIDDEN, NOT_FOUND, CONFLICT, INVALID, PROVIDER }
    private final Code code;
    public CheckoutFailure(Code code) {super(code.name());this.code=code;}
    public Code code() {return code;}

    public ApplicationError toApplicationError() {
        return new ApplicationError("CHECKOUT_" + code.name(), code.name());
    }

    public static CheckoutFailure fromApplicationError(ApplicationError error) {
        for (var code : Code.values()) {
            if (("CHECKOUT_" + code.name()).equals(error.code())) return new CheckoutFailure(code);
        }
        throw new IllegalArgumentException("Unknown checkout error code");
    }
}
