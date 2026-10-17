package quanly.com.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quanly.com.model.PriceSeason;
import quanly.com.model.RoomType;
import quanly.com.repository.PriceSeasonRepository;
import quanly.com.repository.RoomTypeRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class PriceSeasonService {

    @Autowired
    private PriceSeasonRepository priceSeasonRepository;

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    public List<PriceSeason> getByRoomType(Long roomTypeId) {
        return priceSeasonRepository.findByRoomTypeId(roomTypeId);
    }

    public List<PriceSeason> getAll() {
        return priceSeasonRepository.findAll();
    }

    public PriceSeason getById(Long id) {
        return priceSeasonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giá mùa với ID: " + id));
    }

    public PriceSeason save(PriceSeason season) {
        return priceSeasonRepository.save(season);
    }

    public void delete(Long id) {
        priceSeasonRepository.deleteById(id);
    }

    public BigDecimal getDailyPrice(Long roomTypeId, LocalDate date) {
        List<PriceSeason> seasons = priceSeasonRepository.findCurrentSeason(roomTypeId, date);
        if (!seasons.isEmpty() && seasons.get(0).getPrice() != null) {
            return seasons.get(0).getPrice();
        }
        RoomType rt = roomTypeRepository.findById(roomTypeId).orElse(null);
        return (rt != null && rt.getBasePrice() != null) ? rt.getBasePrice() : BigDecimal.ZERO;
    }

    public BigDecimal calculateTotalPrice(Long roomTypeId, LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            return getDailyPrice(roomTypeId, LocalDate.now());
        }

        BigDecimal total = BigDecimal.ZERO;
        LocalDate current = checkIn;
        while (current.isBefore(checkOut)) {
            total = total.add(getDailyPrice(roomTypeId, current));
            current = current.plusDays(1);
        }
        return total;
    }
}
