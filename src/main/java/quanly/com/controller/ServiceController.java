package quanly.com.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import quanly.com.model.ServiceEntity;
import quanly.com.service.AdditionalServiceService;

@Controller
@RequestMapping("/services")
public class ServiceController {

    @Autowired
    private AdditionalServiceService additionalServiceService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("services", additionalServiceService.getAllServices());
        return "service/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("service", new ServiceEntity());
        return "service/form";
    }

    @PostMapping
    public String save(@ModelAttribute("service") ServiceEntity service, RedirectAttributes ra) {
        additionalServiceService.save(service);
        ra.addFlashAttribute("successMessage", "Thêm dịch vụ thành công!");
        return "redirect:/services";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("service", additionalServiceService.getById(id));
        return "service/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable("id") Long id, @ModelAttribute("service") ServiceEntity service, RedirectAttributes ra) {
        additionalServiceService.update(id, service);
        ra.addFlashAttribute("successMessage", "Cập nhật dịch vụ thành công!");
        return "redirect:/services";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes ra) {
        additionalServiceService.delete(id);
        ra.addFlashAttribute("successMessage", "Xóa dịch vụ thành công!");
        return "redirect:/services";
    }
}
