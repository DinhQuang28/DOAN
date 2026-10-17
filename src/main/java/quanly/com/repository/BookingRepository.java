package quanly.com.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import quanly.com.model.Booking;
import quanly.com.model.enums.BookingStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBookingCode(String bookingCode);
    List<Booking> findByGuestId(Long guestId);
    List<Booking> findByStatus(BookingStatus status);
    List<Booking> findByCheckInDateBetween(LocalDate startDate, LocalDate endDate);
    List<Booking> findByCheckOutDateBetween(LocalDate startDate, LocalDate endDate);
    
    long countByStatusAndCheckInDateBetween(BookingStatus status, LocalDate startDate, LocalDate endDate);

    @Query("SELECT b FROM Booking b JOIN b.bookingDetails bd WHERE bd.room.id = :roomId AND b.status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN')")
    List<Booking> findActiveBookingsForRoom(@Param("roomId") Long roomId);
}
