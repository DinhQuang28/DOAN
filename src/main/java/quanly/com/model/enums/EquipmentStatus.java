package quanly.com.model.enums;

public enum EquipmentStatus {
    GOOD("Hoạt động tốt", "bg-success text-white"),
    MAINTENANCE("Đang bảo trì", "bg-warning text-dark"),
    DAMAGED("Hư hỏng", "bg-danger text-white"),
    LOST("Thất lạc / Mất", "bg-secondary text-white");

    private final String displayName;
    private final String badgeClass;

    EquipmentStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}

