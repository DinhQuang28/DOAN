package quanly.com.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import quanly.com.model.Booking;
import quanly.com.model.Guest;
import quanly.com.model.enums.IdType;
import quanly.com.repository.BookingRepository;
import quanly.com.service.GuestService;

import java.util.List;

@Controller
@RequestMapping("/guests")
public class GuestController {

    @Autowired
    private GuestService guestService;

    @Autowired
    private BookingRepository bookingRepository;

    @GetMapping
    public String list(@RequestParam(name = "keyword", required = false) String keyword, Model model) {
        model.addAttribute("guests", guestService.search(keyword));
        model.addAttribute("keyword", keyword);
        return "guest/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        Guest guest = new Guest();
        guest.setIdType(IdType.CCCD);
        guest.setNationality("Việt Nam");
        model.addAttribute("guest", guest);
        model.addAttribute("idTypes", IdType.values());
        return "guest/form";
    }

    @PostMapping
    public String save(@ModelAttribute("guest") Guest guest, RedirectAttributes ra) {
        guestService.save(guest);
        ra.addFlashAttribute("successMessage", "Thêm khách hàng thành công!");
        return "redirect:/guests";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        Guest guest = guestService.getById(id);
        List<Booking> bookings = bookingRepository.findByGuestId(id);
        model.addAttribute("guest", guest);
        model.addAttribute("bookings", bookings);
        return "guest/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("guest", guestService.getById(id));
        model.addAttribute("idTypes", IdType.values());
        return "guest/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable("id") Long id, @ModelAttribute("guest") Guest guest, RedirectAttributes ra) {
        guestService.update(id, guest);
        ra.addFlashAttribute("successMessage", "Cập nhật thông tin khách hàng thành công!");
        return "redirect:/guests/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            guestService.delete(id);
            ra.addFlashAttribute("successMessage", "Xóa khách hàng thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Không thể xóa khách hàng đã có lịch sử đặt phòng!");
        }
        return "redirect:/guests";
    }

    @GetMapping("/api/search")
    @ResponseBody
    public ResponseEntity<List<Guest>> searchApi(@RequestParam("keyword") String keyword) {
        return ResponseEntity.ok(guestService.search(keyword));
    }
}
