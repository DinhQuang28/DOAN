package quanly.com.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import quanly.com.service.BookingService;
import quanly.com.service.ReportService;
import quanly.com.service.RoomService;

@Controller
public class DashboardController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private RoomService roomService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("stats", reportService.getDashboardStats());
        model.addAttribute("recentBookings", bookingService.getRecentBookings(10));
        model.addAttribute("roomMap", roomService.getRoomMapByFloor());
        return "dashboard";
    }
}
