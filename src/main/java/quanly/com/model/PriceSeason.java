package quanly.com.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "price_seasons")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceSeason {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_type_id")
    private RoomType roomType;

    private String seasonName;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal price;
    private String description;
}
