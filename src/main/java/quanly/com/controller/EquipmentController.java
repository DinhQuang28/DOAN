package quanly.com.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import quanly.com.model.Equipment;
import quanly.com.model.enums.EquipmentCategory;
import quanly.com.model.enums.EquipmentStatus;
import quanly.com.service.EquipmentService;
import quanly.com.service.RoomService;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/facilities")
public class EquipmentController {

    @Autowired
    private EquipmentService equipmentService;

    @Autowired
    private RoomService roomService;

    @GetMapping
    public String list(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) EquipmentStatus status,
            @RequestParam(name = "category", required = false) EquipmentCategory category,
            @RequestParam(name = "roomId", required = false) Long roomId,
            Model model) {

        List<Equipment> list = equipmentService.search(keyword, status, category, roomId);

        model.addAttribute("equipments", list);
        model.addAttribute("stats", equipmentService.getStats());
        model.addAttribute("rooms", roomService.getAllRooms());
        model.addAttribute("categories", EquipmentCategory.values());
        model.addAttribute("statuses", EquipmentStatus.values());
        model.addAttribute("selectedKeyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedRoomId", roomId);
        return "facility/list";
    }

    @GetMapping("/new")
    public String createForm(@RequestParam(name = "roomId", required = false) Long roomId, Model model) {
        Equipment equipment = new Equipment();
        equipment.setQuantity(1);
        equipment.setStatus(EquipmentStatus.GOOD);
        equipment.setInstallDate(LocalDate.now());
        if (roomId != null) {
            try {
                equipment.setRoom(roomService.getById(roomId));
            } catch (Exception ignored) {}
        }

        model.addAttribute("equipment", equipment);
        model.addAttribute("rooms", roomService.getAllRooms());
        model.addAttribute("categories", EquipmentCategory.values());
        model.addAttribute("statuses", EquipmentStatus.values());
        return "facility/form";
    }

    @PostMapping
    public String save(@ModelAttribute("equipment") Equipment equipment, RedirectAttributes ra) {
        try {
            equipmentService.save(equipment);
            ra.addFlashAttribute("successMessage", "Thêm trang thiết bị thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/facilities";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model) {
        Equipment equipment = equipmentService.getById(id);
        model.addAttribute("equipment", equipment);
        model.addAttribute("rooms", roomService.getAllRooms());
        model.addAttribute("categories", EquipmentCategory.values());
        model.addAttribute("statuses", EquipmentStatus.values());
        return "facility/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable("id") Long id, @ModelAttribute("equipment") Equipment equipment, RedirectAttributes ra) {
        try {
            equipmentService.update(id, equipment);
            ra.addFlashAttribute("successMessage", "Cập nhật trang thiết bị thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/facilities";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            equipmentService.delete(id);
            ra.addFlashAttribute("successMessage", "Xóa trang thiết bị thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/facilities";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") EquipmentStatus status,
            @RequestParam(name = "notes", required = false) String notes,
            RedirectAttributes ra) {
        try {
            equipmentService.updateStatus(id, status, notes);
            ra.addFlashAttribute("successMessage", "Cập nhật trạng thái trang thiết bị thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/facilities";
    }
}

