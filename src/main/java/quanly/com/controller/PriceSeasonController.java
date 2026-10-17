package quanly.com.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import quanly.com.model.PriceSeason;
import quanly.com.service.PriceSeasonService;
import quanly.com.service.RoomTypeService;

@Controller
@RequestMapping("/price-seasons")
public class PriceSeasonController {

    @Autowired
    private PriceSeasonService priceSeasonService;

    @Autowired
    private RoomTypeService roomTypeService;

    @GetMapping
    public String list(@RequestParam(name = "roomTypeId", required = false) Long roomTypeId, Model model) {
        if (roomTypeId != null) {
            model.addAttribute("seasons", priceSeasonService.getByRoomType(roomTypeId));
        } else {
            model.addAttribute("seasons", priceSeasonService.getAll());
        }
        model.addAttribute("roomTypes", roomTypeService.getAllRoomTypes());
        model.addAttribute("selectedTypeId", roomTypeId);
        return "priceseason/list";
    }

    @GetMapping("/new")
    public String createForm(@RequestParam(name = "roomTypeId", required = false) Long roomTypeId, Model model) {
        PriceSeason season = new PriceSeason();
        if (roomTypeId != null) {
            season.setRoomType(roomTypeService.getById(roomTypeId));
        }
        model.addAttribute("season", season);
        model.addAttribute("roomTypes", roomTypeService.getAllRoomTypes());
        return "priceseason/form";
    }

    @PostMapping
    public String save(@ModelAttribute("season") PriceSeason season, RedirectAttributes ra) {
        priceSeasonService.save(season);
        ra.addFlashAttribute("successMessage", "Thêm giá theo mùa thành công!");
        return "redirect:/price-seasons";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("season", priceSeasonService.getById(id));
        model.addAttribute("roomTypes", roomTypeService.getAllRoomTypes());
        return "priceseason/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable("id") Long id, @ModelAttribute("season") PriceSeason season, RedirectAttributes ra) {
        season.setId(id);
        priceSeasonService.save(season);
        ra.addFlashAttribute("successMessage", "Cập nhật giá theo mùa thành công!");
        return "redirect:/price-seasons";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes ra) {
        priceSeasonService.delete(id);
        ra.addFlashAttribute("successMessage", "Xóa giá theo mùa thành công!");
        return "redirect:/price-seasons";
    }
}
