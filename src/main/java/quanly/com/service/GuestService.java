package quanly.com.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quanly.com.model.Guest;
import quanly.com.repository.GuestRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class GuestService {

    @Autowired
    private GuestRepository guestRepository;

    public List<Guest> getAllGuests() {
        return guestRepository.findAll();
    }

    public Guest getById(Long id) {
        return guestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khách hàng với ID: " + id));
    }

    public Optional<Guest> findByIdNumber(String idNumber) {
        if (idNumber == null || idNumber.trim().isEmpty()) return Optional.empty();
        return guestRepository.findByIdNumber(idNumber.trim());
    }

    public Optional<Guest> findByPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return Optional.empty();
        return guestRepository.findByPhone(phone.trim());
    }

    public List<Guest> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllGuests();
        }
        String kw = keyword.trim().toLowerCase();
        return guestRepository.findAll().stream()
                .filter(g -> (g.getFullName() != null && g.getFullName().toLowerCase().contains(kw))
                        || (g.getPhone() != null && g.getPhone().contains(kw))
                        || (g.getIdNumber() != null && g.getIdNumber().contains(kw))
                        || (g.getEmail() != null && g.getEmail().toLowerCase().contains(kw)))
                .collect(Collectors.toList());
    }

    public Guest save(Guest guest) {
        return guestRepository.save(guest);
    }

    public Guest update(Long id, Guest updated) {
        Guest existing = getById(id);
        existing.setFullName(updated.getFullName());
        existing.setIdNumber(updated.getIdNumber());
        existing.setIdType(updated.getIdType());
        existing.setPhone(updated.getPhone());
        existing.setEmail(updated.getEmail());
        existing.setAddress(updated.getAddress());
        existing.setNationality(updated.getNationality());
        existing.setNotes(updated.getNotes());
        return guestRepository.save(existing);
    }

    public void delete(Long id) {
        guestRepository.deleteById(id);
    }

    public void addLoyaltyPoints(Long guestId, int points) {
        Guest guest = getById(guestId);
        guest.setLoyaltyPoints(guest.getLoyaltyPoints() + points);
        guestRepository.save(guest);
    }
}
