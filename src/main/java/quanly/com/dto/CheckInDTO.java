package quanly.com.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckInDTO {
    private Long bookingId;
    private String guestIdNumber;
    private String notes;
}
