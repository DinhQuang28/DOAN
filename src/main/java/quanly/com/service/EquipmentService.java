package quanly.com.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quanly.com.model.Equipment;
import quanly.com.model.enums.EquipmentCategory;
import quanly.com.model.enums.EquipmentStatus;
import quanly.com.repository.EquipmentRepository;
import quanly.com.repository.PropertyRepository;
import quanly.com.repository.RoomRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional
public class EquipmentService {

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    public List<Equipment> getAll() {
        return equipmentRepository.findAll();
    }

    public Equipment getById(Long id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy trang thiết bị có ID: " + id));
    }

    public Equipment save(Equipment equipment) {
        // Tự động sinh mã nếu người dùng không nhập mã
        if (equipment.getCode() == null || equipment.getCode().trim().isEmpty()) {
            String prefix = "CSVC-";
            if (equipment.getRoom() != null && equipment.getRoom().getRoomNumber() != null) {
                prefix = "TB" + equipment.getRoom().getRoomNumber() + "-";
            }
            String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMM"));
            int rand = ThreadLocalRandom.current().nextInt(100, 999);
            equipment.setCode(prefix + datePart + rand);
        }

        if (equipment.getProperty() == null && propertyRepository.count() > 0) {
            equipment.setProperty(propertyRepository.findAll().get(0));
        }

        return equipmentRepository.save(equipment);
    }

    public Equipment update(Long id, Equipment data) {
        Equipment existing = getById(id);
        existing.setName(data.getName());
        if (data.getCode() != null && !data.getCode().trim().isEmpty()) {
            existing.setCode(data.getCode().trim());
        }
        existing.setCategory(data.getCategory());
        existing.setStatus(data.getStatus());
        existing.setQuantity(data.getQuantity() > 0 ? data.getQuantity() : 1);
        existing.setPurchasePrice(data.getPurchasePrice());
        existing.setInstallDate(data.getInstallDate());
        existing.setWarrantyExpiry(data.getWarrantyExpiry());
        existing.setNotes(data.getNotes());
        existing.setRoom(data.getRoom());

        return equipmentRepository.save(existing);
    }

    public void delete(Long id) {
        Equipment existing = getById(id);
        equipmentRepository.delete(existing);
    }

    public void updateStatus(Long id, EquipmentStatus status, String notes) {
        Equipment existing = getById(id);
        existing.setStatus(status);
        if (notes != null && !notes.trim().isEmpty()) {
            String currNotes = existing.getNotes() != null ? existing.getNotes() : "";
            existing.setNotes(currNotes + (currNotes.isEmpty() ? "" : " | ") + notes.trim());
        }
        equipmentRepository.save(existing);
    }

    public List<Equipment> search(String keyword, EquipmentStatus status, EquipmentCategory category, Long roomId) {
        return equipmentRepository.search(keyword, status, category, roomId);
    }

    public List<Equipment> getByRoomId(Long roomId) {
        return equipmentRepository.findByRoomId(roomId);
    }

    public Map<String, Object> getStats() {
        List<Equipment> all = equipmentRepository.findAll();
        Map<String, Object> stats = new HashMap<>();

        int totalItems = all.size();
        int totalQuantity = 0;
        int goodCount = 0;
        int maintenanceCount = 0;
        int damagedCount = 0;
        int lostCount = 0;
        BigDecimal totalValue = BigDecimal.ZERO;

        for (Equipment e : all) {
            int q = e.getQuantity() > 0 ? e.getQuantity() : 1;
            totalQuantity += q;

            if (e.getStatus() == EquipmentStatus.GOOD) {
                goodCount += q;
            } else if (e.getStatus() == EquipmentStatus.MAINTENANCE) {
                maintenanceCount += q;
            } else if (e.getStatus() == EquipmentStatus.DAMAGED) {
                damagedCount += q;
            } else if (e.getStatus() == EquipmentStatus.LOST) {
                lostCount += q;
            }

            if (e.getPurchasePrice() != null) {
                totalValue = totalValue.add(e.getPurchasePrice().multiply(BigDecimal.valueOf(q)));
            }
        }

        stats.put("totalItems", totalItems);
        stats.put("totalQuantity", totalQuantity);
        stats.put("goodCount", goodCount);
        stats.put("maintenanceCount", maintenanceCount);
        stats.put("damagedCount", damagedCount);
        stats.put("lostCount", lostCount);
        stats.put("totalValue", totalValue);

        return stats;
    }
}

