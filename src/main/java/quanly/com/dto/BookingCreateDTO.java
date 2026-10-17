package quanly.com.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import quanly.com.model.enums.BookingChannel;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class BookingCreateDTO {
    private Long guestId;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkInDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkOutDate;

    private List<Long> roomIds;
    private int numGuests;
    private BookingChannel channel;
    private String promotionCode;
    private String notes;
}
