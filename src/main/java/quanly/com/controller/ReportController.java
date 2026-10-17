package quanly.com.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import quanly.com.service.ReportService;

@Controller
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("stats", reportService.getDashboardStats());
        model.addAttribute("dailyRevenue", reportService.getRevenueLast7Days());
        model.addAttribute("roomTypeRevenue", reportService.getRevenueByRoomType());
        model.addAttribute("channelStats", reportService.getBookingChannelStats());
        return "report/dashboard";
    }
}
