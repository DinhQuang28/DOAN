package quanly.com.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class RevenueReportDTO {
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    
    private BigDecimal revenue;
    private int bookingCount;
}
