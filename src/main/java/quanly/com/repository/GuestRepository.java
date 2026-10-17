package quanly.com.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import quanly.com.model.Guest;

import java.util.List;
import java.util.Optional;

@Repository
public interface GuestRepository extends JpaRepository<Guest, Long> {
    Optional<Guest> findByIdNumber(String idNumber);
    Optional<Guest> findByPhone(String phone);
    Optional<Guest> findByEmail(String email);
    List<Guest> findByFullNameContainingIgnoreCase(String fullName);
}
