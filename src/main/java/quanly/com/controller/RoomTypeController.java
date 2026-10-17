package quanly.com.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import quanly.com.model.RoomType;
import quanly.com.service.RoomTypeService;

@Controller
@RequestMapping("/room-types")
public class RoomTypeController {

    @Autowired
    private RoomTypeService roomTypeService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("roomTypes", roomTypeService.getAllRoomTypes());
        return "roomtype/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("roomType", new RoomType());
        return "roomtype/form";
    }

    @PostMapping
    public String save(@ModelAttribute("roomType") RoomType roomType, RedirectAttributes ra) {
        roomTypeService.save(roomType);
        ra.addFlashAttribute("successMessage", "Thêm loại phòng thành công!");
        return "redirect:/room-types";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("roomType", roomTypeService.getById(id));
        return "roomtype/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable("id") Long id, @ModelAttribute("roomType") RoomType roomType, RedirectAttributes ra) {
        roomTypeService.update(id, roomType);
        ra.addFlashAttribute("successMessage", "Cập nhật loại phòng thành công!");
        return "redirect:/room-types";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            roomTypeService.delete(id);
            ra.addFlashAttribute("successMessage", "Xóa loại phòng thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Không thể xóa loại phòng đang có phòng liên kết!");
        }
        return "redirect:/room-types";
    }
}
