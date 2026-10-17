package quanly.com.dto;

import lombok.Builder;
import lombok.Data;
import quanly.com.model.enums.IdType;

@Data
@Builder
public class GuestDTO {
    private String fullName;
    private String idNumber;
    private IdType idType;
    private String phone;
    private String email;
    private String address;
    private String nationality;
}
