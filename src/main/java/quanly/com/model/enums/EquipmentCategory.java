package quanly.com.model.enums;

public enum EquipmentCategory {
    ELECTRONICS("Điện máy - Điện tử"),
    FURNITURE("Nội thất - Đồ gỗ"),
    BATHROOM("Thiết bị phòng tắm"),
    BEDDING("Tiện nghi phòng ngủ"),
    GENERAL("Cơ sở vật chất chung");

    private final String displayName;

    EquipmentCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

