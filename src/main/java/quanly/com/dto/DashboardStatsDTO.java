package quanly.com.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DashboardStatsDTO {
    private int totalRooms;
    private int availableRooms;
    private int occupiedRooms;
    private int todayCheckIns;
    private int todayCheckOuts;
    private BigDecimal monthlyRevenue;
    private double occupancyRate;
}
