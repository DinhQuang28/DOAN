package quanly.com.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import quanly.com.model.Promotion;
import quanly.com.service.PromotionService;

@Controller
@RequestMapping("/promotions")
public class PromotionController {

    @Autowired
    private PromotionService promotionService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("promotions", promotionService.getAllPromotions());
        return "promotion/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        Promotion promo = new Promotion();
        promo.setType("PERCENTAGE");
        promo.setActive(true);
        model.addAttribute("promotion", promo);
        return "promotion/form";
    }

    @PostMapping
    public String save(@ModelAttribute("promotion") Promotion promo, RedirectAttributes ra) {
        promotionService.save(promo);
        ra.addFlashAttribute("successMessage", "Thêm mã khuyến mãi thành công!");
        return "redirect:/promotions";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("promotion", promotionService.getById(id));
        return "promotion/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable("id") Long id, @ModelAttribute("promotion") Promotion promo, RedirectAttributes ra) {
        promo.setId(id);
        promotionService.save(promo);
        ra.addFlashAttribute("successMessage", "Cập nhật khuyến mãi thành công!");
        return "redirect:/promotions";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes ra) {
        promotionService.delete(id);
        ra.addFlashAttribute("successMessage", "Xóa khuyến mãi thành công!");
        return "redirect:/promotions";
    }
}
