package quanly.com.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import quanly.com.model.Room;
import quanly.com.model.enums.RoomStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByStatus(RoomStatus status);
    List<Room> findByPropertyId(Long propertyId);
    List<Room> findByRoomTypeId(Long roomTypeId);
    Optional<Room> findByRoomNumber(String roomNumber);
    List<Room> findByPropertyIdAndStatus(Long propertyId, RoomStatus status);

    @Query("SELECT r FROM Room r WHERE r.id NOT IN (" +
           "SELECT bd.room.id FROM BookingDetail bd " +
           "JOIN bd.booking b " +
           "WHERE b.status NOT IN ('CANCELLED', 'CHECKED_OUT') " +
           "AND (b.checkInDate < :checkOutDate AND b.checkOutDate > :checkInDate))")
    List<Room> findAvailableRooms(@Param("checkInDate") LocalDate checkInDate, 
                                  @Param("checkOutDate") LocalDate checkOutDate);
}
