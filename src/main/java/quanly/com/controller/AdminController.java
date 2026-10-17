package quanly.com.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import quanly.com.model.User;
import quanly.com.model.enums.UserRole;
import quanly.com.repository.PropertyRepository;
import quanly.com.service.UserService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private PropertyRepository propertyRepository;

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }

    @GetMapping("/users/new")
    public String createUserForm(Model model) {
        User user = new User();
        user.setRole(UserRole.ROLE_RECEPTIONIST);
        user.setEnabled(true);
        model.addAttribute("user", user);
        model.addAttribute("roles", UserRole.values());
        return "admin/user_form";
    }

    @PostMapping("/users")
    public String saveUser(@ModelAttribute("user") User user, RedirectAttributes ra) {
        if (propertyRepository.count() > 0) {
            user.setProperty(propertyRepository.findAll().get(0));
        }
        userService.save(user);
        ra.addFlashAttribute("successMessage", "Thêm người dùng thành công!");
        return "redirect:/admin/users";
    }

    @GetMapping("/users/{id}/edit")
    public String editUserForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("user", userService.getById(id));
        model.addAttribute("roles", UserRole.values());
        return "admin/user_form";
    }

    @PostMapping("/users/{id}")
    public String updateUser(@PathVariable("id") Long id, @ModelAttribute("user") User user, RedirectAttributes ra) {
        userService.update(id, user);
        ra.addFlashAttribute("successMessage", "Cập nhật người dùng thành công!");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            userService.delete(id);
            ra.addFlashAttribute("successMessage", "Xóa người dùng thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Không thể xóa người dùng: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/audit-logs")
    public String auditLogs(Model model) {
        model.addAttribute("logs", userService.getRecentAuditLogs());
        return "admin/audit_logs";
    }
}
