package quanly.com.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import quanly.com.model.Booking;
import quanly.com.model.Payment;
import quanly.com.model.Property;
import quanly.com.model.enums.BookingStatus;
import quanly.com.model.enums.PaymentMethod;
import quanly.com.model.enums.PaymentStatus;
import quanly.com.repository.BookingDetailRepository;
import quanly.com.repository.PropertyRepository;
import quanly.com.repository.ServiceUsageRepository;
import quanly.com.service.BookingService;
import quanly.com.service.PaymentService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private BookingDetailRepository bookingDetailRepository;

    @Autowired
    private ServiceUsageRepository serviceUsageRepository;

    @GetMapping
    public String list(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "method", required = false) PaymentMethod method,
            @RequestParam(name = "status", required = false) PaymentStatus status,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            Model model) {

        List<Payment> payments = paymentService.filterPayments(keyword, method, status, startDate, endDate);

        BigDecimal totalRevenue = paymentService.getTotalRevenue();
        BigDecimal todayTotal = paymentService.getTodayTotal();
        long totalSuccess = payments.stream().filter(p -> p.getStatus() == PaymentStatus.COMPLETED).count();
        long totalRefunded = payments.stream().filter(p -> p.getStatus() == PaymentStatus.REFUNDED).count();

        model.addAttribute("payments", payments);
        model.addAttribute("methods", PaymentMethod.values());
        model.addAttribute("statuses", PaymentStatus.values());
        model.addAttribute("selectedMethod", method);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("todayTotal", todayTotal);
        model.addAttribute("totalSuccess", totalSuccess);
        model.addAttribute("totalRefunded", totalRefunded);

        return "payment/list";
    }

    @GetMapping("/new")
    public String createForm(
            @RequestParam(name = "bookingId", required = false) Long bookingId,
            Model model) {

        // Danh sách các booking đang còn khả dụng để thanh toán (trừ đã hủy)
        List<Booking> activeBookings = bookingService.getAllBookings().stream()
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                .sorted((a, b) -> Long.compare(b.getId() != null ? b.getId() : 0, a.getId() != null ? a.getId() : 0))
                .collect(Collectors.toList());

        Booking selectedBooking = null;
        BigDecimal totalCharge = BigDecimal.ZERO;
        BigDecimal alreadyPaid = BigDecimal.ZERO;
        BigDecimal balanceDue = BigDecimal.ZERO;

        if (bookingId != null) {
            try {
                selectedBooking = bookingService.getById(bookingId);
                totalCharge = bookingService.calculateTotalCharges(bookingId);
                alreadyPaid = paymentService.getTotalPaidForBooking(bookingId);
                balanceDue = totalCharge.subtract(alreadyPaid).max(BigDecimal.ZERO);
            } catch (Exception ignored) {}
        }

        Property property = propertyRepository.findAll().stream().findFirst().orElse(null);

        model.addAttribute("activeBookings", activeBookings);
        model.addAttribute("selectedBooking", selectedBooking);
        model.addAttribute("bookingId", bookingId);
        model.addAttribute("totalCharge", totalCharge);
        model.addAttribute("alreadyPaid", alreadyPaid);
        model.addAttribute("balanceDue", balanceDue);
        model.addAttribute("methods", PaymentMethod.values());
        model.addAttribute("property", property);

        return "payment/form";
    }

    @PostMapping
    public String savePayment(
            @RequestParam("bookingId") Long bookingId,
            @RequestParam("amount") BigDecimal amount,
            @RequestParam("method") PaymentMethod method,
            @RequestParam(name = "transactionRef", required = false) String transactionRef,
            @RequestParam(name = "notes", required = false) String notes,
            RedirectAttributes ra) {

        try {
            Payment payment = paymentService.processPayment(bookingId, amount, method, transactionRef, notes);
            ra.addFlashAttribute("successMessage", "Tạo phiếu thu thành công! Mã giao dịch: " + payment.getTransactionRef());
            return "redirect:/payments/" + payment.getId();
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi tạo phiếu thanh toán: " + e.getMessage());
            return "redirect:/payments/new?bookingId=" + bookingId;
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        Payment payment = paymentService.getById(id);
        Booking booking = payment.getBooking();
        Property property = propertyRepository.findAll().stream().findFirst().orElse(null);

        BigDecimal currentTotal = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal balanceDue = BigDecimal.ZERO;

        if (booking != null) {
            currentTotal = bookingService.calculateTotalCharges(booking.getId());
            totalPaid = paymentService.getTotalPaidForBooking(booking.getId());
            balanceDue = currentTotal.subtract(totalPaid).max(BigDecimal.ZERO);
            model.addAttribute("bookingDetails", bookingDetailRepository.findByBookingId(booking.getId()));
            model.addAttribute("serviceUsageRepo", serviceUsageRepository);
        }

        model.addAttribute("payment", payment);
        model.addAttribute("booking", booking);
        model.addAttribute("property", property);
        model.addAttribute("currentTotal", currentTotal);
        model.addAttribute("totalPaid", totalPaid);
        model.addAttribute("balanceDue", balanceDue);

        return "payment/detail";
    }

    @PostMapping("/{id}/refund")
    public String refund(
            @PathVariable("id") Long id,
            @RequestParam(name = "reason", required = false) String reason,
            RedirectAttributes ra) {

        try {
            paymentService.refundPayment(id, reason);
            ra.addFlashAttribute("successMessage", "Đã xử lý hoàn tiền thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi hoàn tiền: " + e.getMessage());
        }
        return "redirect:/payments/" + id;
    }
}
