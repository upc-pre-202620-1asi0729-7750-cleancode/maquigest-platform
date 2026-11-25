package com.maquigest.platform.subscriptions.application.commandservices;
public final class CheckoutFailure extends RuntimeException {
    public enum Code { UNAVAILABLE, FORBIDDEN, NOT_FOUND, CONFLICT, INVALID, PROVIDER }
    private final Code code;
    public CheckoutFailure(Code code) {super(code.name());this.code=code;}
    public Code code() {return code;}
}
