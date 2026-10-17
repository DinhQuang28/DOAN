package quanly.com.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quanly.com.model.Room;
import quanly.com.model.enums.RoomStatus;
import quanly.com.repository.RoomRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired(required = false)
    private SimpMessagingTemplate messagingTemplate;

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Room getById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng với ID: " + id));
    }

    public Room save(Room room) {
        return roomRepository.save(room);
    }

    public Room update(Long id, Room updated) {
        Room existing = getById(id);
        existing.setRoomNumber(updated.getRoomNumber());
        existing.setFloor(updated.getFloor());
        existing.setRoomType(updated.getRoomType());
        existing.setStatus(updated.getStatus());
        existing.setNotes(updated.getNotes());
        return roomRepository.save(existing);
    }

    public void delete(Long id) {
        roomRepository.deleteById(id);
    }

    public List<Room> getRoomsByStatus(RoomStatus status) {
        return roomRepository.findByStatus(status);
    }

    public List<Room> getRoomsByType(Long roomTypeId) {
        return roomRepository.findByRoomTypeId(roomTypeId);
    }

    public List<Room> getRoomsByFloor(int floor) {
        return roomRepository.findAll().stream()
                .filter(r -> r.getFloor() == floor)
                .sorted(Comparator.comparing(Room::getRoomNumber))
                .collect(Collectors.toList());
    }

    public Map<Integer, List<Room>> getRoomMapByFloor() {
        Map<Integer, List<Room>> map = new TreeMap<>();
        List<Room> allRooms = roomRepository.findAll();
        for (Room r : allRooms) {
            map.computeIfAbsent(r.getFloor(), k -> new ArrayList<>()).add(r);
        }
        for (List<Room> list : map.values()) {
            list.sort(Comparator.comparing(Room::getRoomNumber));
        }
        return map;
    }

    public void updateRoomStatus(Long roomId, RoomStatus newStatus) {
        Room room = getById(roomId);
        room.setStatus(newStatus);
        roomRepository.save(room);

        // Send realtime update over WebSocket if template available
        if (messagingTemplate != null) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("roomId", room.getId());
            payload.put("roomNumber", room.getRoomNumber());
            payload.put("status", newStatus.name());
            try {
                messagingTemplate.convertAndSend("/topic/room-status", (Object) payload);
            } catch (Exception ignored) {
            }
        }
    }

    public List<Room> getAvailableRooms(LocalDate checkIn, LocalDate checkOut, Long roomTypeId, Integer guests) {
        if (checkIn == null) checkIn = LocalDate.now();
        if (checkOut == null) checkOut = checkIn.plusDays(1);

        List<Room> available = roomRepository.findAvailableRooms(checkIn, checkOut);

        return available.stream()
                .filter(r -> roomTypeId == null || (r.getRoomType() != null && r.getRoomType().getId().equals(roomTypeId)))
                .filter(r -> guests == null || (r.getRoomType() != null && r.getRoomType().getMaxGuests() >= guests))
                .collect(Collectors.toList());
    }

    public long countTotal() {
        return roomRepository.count();
    }

    public long countByStatus(RoomStatus status) {
        return roomRepository.findByStatus(status).size();
    }
}
