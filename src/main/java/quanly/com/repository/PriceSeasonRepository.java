package quanly.com.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import quanly.com.model.PriceSeason;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PriceSeasonRepository extends JpaRepository<PriceSeason, Long> {
    List<PriceSeason> findByRoomTypeId(Long roomTypeId);

    @Query("SELECT ps FROM PriceSeason ps WHERE ps.roomType.id = :roomTypeId AND ps.startDate <= :date AND ps.endDate >= :date")
    List<PriceSeason> findCurrentSeason(@Param("roomTypeId") Long roomTypeId, @Param("date") LocalDate date);
}
