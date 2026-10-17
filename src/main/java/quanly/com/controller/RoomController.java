package quanly.com.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import quanly.com.model.Room;
import quanly.com.model.enums.RoomStatus;
import quanly.com.repository.PropertyRepository;
import quanly.com.service.RoomService;
import quanly.com.service.RoomTypeService;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomTypeService roomTypeService;

    @Autowired
    private PropertyRepository propertyRepository;

    @GetMapping
    public String list(
            @RequestParam(name = "status", required = false) RoomStatus status,
            @RequestParam(name = "floor", required = false) Integer floor,
            @RequestParam(name = "typeId", required = false) Long typeId,
            Model model) {

        List<Room> rooms = roomService.getAllRooms();
        if (status != null) {
            rooms = rooms.stream().filter(r -> r.getStatus() == status).collect(Collectors.toList());
        }
        if (floor != null) {
            rooms = rooms.stream().filter(r -> r.getFloor() == floor).collect(Collectors.toList());
        }
        if (typeId != null) {
            rooms = rooms.stream().filter(r -> r.getRoomType() != null && r.getRoomType().getId().equals(typeId)).collect(Collectors.toList());
        }

        model.addAttribute("rooms", rooms);
        model.addAttribute("roomTypes", roomTypeService.getAllRoomTypes());
        model.addAttribute("statuses", RoomStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedFloor", floor);
        model.addAttribute("selectedTypeId", typeId);
        return "room/list";
    }

    @GetMapping("/map")
    public String floorMap(Model model) {
        model.addAttribute("roomMap", roomService.getRoomMapByFloor());
        model.addAttribute("statuses", RoomStatus.values());
        return "room/map";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("room", new Room());
        model.addAttribute("roomTypes", roomTypeService.getAllRoomTypes());
        model.addAttribute("statuses", RoomStatus.values());
        return "room/form";
    }

    @PostMapping
    public String save(@ModelAttribute("room") Room room, RedirectAttributes ra) {
        if (propertyRepository.count() > 0) {
            room.setProperty(propertyRepository.findAll().get(0));
        }
        roomService.save(room);
        ra.addFlashAttribute("successMessage", "Thêm phòng thành công!");
        return "redirect:/rooms";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("room", roomService.getById(id));
        model.addAttribute("roomTypes", roomTypeService.getAllRoomTypes());
        model.addAttribute("statuses", RoomStatus.values());
        return "room/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable("id") Long id, @ModelAttribute("room") Room room, RedirectAttributes ra) {
        roomService.update(id, room);
        ra.addFlashAttribute("successMessage", "Cập nhật phòng thành công!");
        return "redirect:/rooms";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") RoomStatus status,
            @RequestParam(name = "redirect", defaultValue = "map") String redirect,
            RedirectAttributes ra) {
        roomService.updateRoomStatus(id, status);
        ra.addFlashAttribute("successMessage", "Đã cập nhật trạng thái phòng!");
        return "redirect:/rooms/" + ("list".equals(redirect) ? "" : "map");
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            roomService.delete(id);
            ra.addFlashAttribute("successMessage", "Xóa phòng thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Không thể xóa phòng có dữ liệu đặt phòng liên kết!");
        }
        return "redirect:/rooms";
    }
}
