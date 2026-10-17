package quanly.com.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quanly.com.model.Promotion;
import quanly.com.repository.PromotionRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PromotionService {

    @Autowired
    private PromotionRepository promotionRepository;

    public List<Promotion> getAllPromotions() {
        return promotionRepository.findAll();
    }

    public List<Promotion> getActivePromotions() {
        return promotionRepository.findActivePromotions(LocalDate.now());
    }

    public Promotion getById(Long id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy mã khuyến mãi với ID: " + id));
    }

    public Optional<Promotion> findByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return Optional.empty();
        }
        return promotionRepository.findByCode(code.trim().toUpperCase());
    }

    public Promotion save(Promotion promotion) {
        if (promotion.getCode() != null) {
            promotion.setCode(promotion.getCode().trim().toUpperCase());
        }
        return promotionRepository.save(promotion);
    }

    public void delete(Long id) {
        promotionRepository.deleteById(id);
    }

    public BigDecimal calculateDiscount(String code, BigDecimal totalAmount) {
        if (code == null || code.trim().isEmpty() || totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        Optional<Promotion> opt = findByCode(code);
        if (opt.isEmpty()) return BigDecimal.ZERO;

        Promotion promo = opt.get();
        if (!promo.isActive()) return BigDecimal.ZERO;

        LocalDate now = LocalDate.now();
        if (promo.getStartDate() != null && now.isBefore(promo.getStartDate())) return BigDecimal.ZERO;
        if (promo.getEndDate() != null && now.isAfter(promo.getEndDate())) return BigDecimal.ZERO;

        if (promo.getMaxUses() > 0 && promo.getCurrentUses() >= promo.getMaxUses()) return BigDecimal.ZERO;

        if ("PERCENTAGE".equalsIgnoreCase(promo.getType())) {
            BigDecimal percent = promo.getValue().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
            return totalAmount.multiply(percent).setScale(0, RoundingMode.HALF_UP);
        } else if ("FIXED".equalsIgnoreCase(promo.getType())) {
            return promo.getValue().min(totalAmount);
        }

        return BigDecimal.ZERO;
    }

    public void incrementUsage(String code) {
        findByCode(code).ifPresent(p -> {
            p.setCurrentUses(p.getCurrentUses() + 1);
            promotionRepository.save(p);
        });
    }
}
