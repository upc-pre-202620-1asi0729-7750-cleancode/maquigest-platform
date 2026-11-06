package com.maquigest.platform.inventory.interfaces.acl;

/** Provider-owned failure that Maintenance translates to its existing HTTP/application codes. */
public final class InventoryMaintenanceFailure extends RuntimeException {
    public enum Kind { INVALID, NOT_FOUND, FORBIDDEN, CONFLICT }
    private final Kind kind;
    private InventoryMaintenanceFailure(Kind kind, String message) {
        super(message);
        this.kind = kind;
    }
    public Kind kind() { return kind; }
    public static InventoryMaintenanceFailure invalid(String message) {
        return new InventoryMaintenanceFailure(Kind.INVALID, message);
    }
    public static InventoryMaintenanceFailure notFound() {
        return new InventoryMaintenanceFailure(Kind.NOT_FOUND, "Equipment");
    }
    public static InventoryMaintenanceFailure forbidden() {
        return new InventoryMaintenanceFailure(Kind.FORBIDDEN, "Forbidden");
    }
    public static InventoryMaintenanceFailure conflict(String message) {
        return new InventoryMaintenanceFailure(Kind.CONFLICT, message);
    }
}
