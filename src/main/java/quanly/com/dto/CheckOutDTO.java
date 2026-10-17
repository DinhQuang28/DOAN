package quanly.com.dto;

import lombok.Builder;
import lombok.Data;
import quanly.com.model.enums.PaymentMethod;

@Data
@Builder
public class CheckOutDTO {
    private Long bookingId;
    private PaymentMethod paymentMethod;
    private String notes;
}
