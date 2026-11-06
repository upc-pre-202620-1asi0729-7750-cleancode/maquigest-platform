package com.maquigest.platform.inventory.interfaces.acl;

/** Context-neutral failure; consuming BC maps it to its own REST/application error vocabulary. */
public final class InventoryRentalFailure extends RuntimeException {
    public enum Kind { INVALID, NOT_FOUND, FORBIDDEN, CONFLICT }

    private final Kind kind;

    private InventoryRentalFailure(Kind kind, String message) {
        super(message);
        this.kind = kind;
    }
    public Kind kind() { return kind; }
    public static InventoryRentalFailure invalid(String message) {
        return new InventoryRentalFailure(Kind.INVALID, message);
    }
    public static InventoryRentalFailure notFound() {
        return new InventoryRentalFailure(Kind.NOT_FOUND, "Equipment");
    }
    public static InventoryRentalFailure forbidden() {
        return new InventoryRentalFailure(Kind.FORBIDDEN, "Forbidden");
    }
    public static InventoryRentalFailure conflict(String message) {
        return new InventoryRentalFailure(Kind.CONFLICT, message);
    }
}
