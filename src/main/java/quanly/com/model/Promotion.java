package quanly.com.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "promotions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String code;
    
    private String name;
    private String description;
    
    private String type; // PERCENTAGE or FIXED
    private BigDecimal value;
    
    private Integer minStay;
    
    private LocalDate startDate;
    private LocalDate endDate;
    
    private int maxUses;
    
    @Builder.Default
    private int currentUses = 0;
    
    @Builder.Default
    private boolean active = true;
}
