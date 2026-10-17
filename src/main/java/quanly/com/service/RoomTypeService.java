package quanly.com.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quanly.com.model.RoomType;
import quanly.com.repository.RoomTypeRepository;

import java.util.List;

@Service
@Transactional
public class RoomTypeService {

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    public List<RoomType> getAllRoomTypes() {
        return roomTypeRepository.findAll();
    }

    public RoomType getById(Long id) {
        return roomTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy loại phòng với ID: " + id));
    }

    public RoomType save(RoomType roomType) {
        return roomTypeRepository.save(roomType);
    }

    public RoomType update(Long id, RoomType updated) {
        RoomType existing = getById(id);
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setMaxGuests(updated.getMaxGuests());
        existing.setBasePrice(updated.getBasePrice());
        existing.setAmenities(updated.getAmenities());
        existing.setImageUrl(updated.getImageUrl());
        return roomTypeRepository.save(existing);
    }

    public void delete(Long id) {
        roomTypeRepository.deleteById(id);
    }

    public long count() {
        return roomTypeRepository.count();
    }
}
