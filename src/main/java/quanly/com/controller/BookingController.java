package quanly.com.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import quanly.com.dto.BookingCreateDTO;
import quanly.com.dto.CheckInDTO;
import quanly.com.dto.CheckOutDTO;
import quanly.com.dto.RoomSearchDTO;
import quanly.com.model.Booking;
import quanly.com.model.BookingDetail;
import quanly.com.model.Guest;
import quanly.com.model.Room;
import quanly.com.model.enums.BookingChannel;
import quanly.com.model.enums.BookingStatus;
import quanly.com.model.enums.PaymentMethod;
import quanly.com.repository.BookingDetailRepository;
import quanly.com.repository.PaymentRepository;
import quanly.com.repository.ServiceUsageRepository;
import quanly.com.service.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomTypeService roomTypeService;

    @Autowired
    private GuestService guestService;

    @Autowired
    private AdditionalServiceService additionalServiceService;

    @Autowired
    private BookingDetailRepository bookingDetailRepository;

    @Autowired
    private ServiceUsageRepository serviceUsageRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentService paymentService;

    @GetMapping
    public String list(
            @RequestParam(name = "status", required = false) BookingStatus status,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            Model model) {

        List<Booking> list = bookingService.getAllBookings();

        if (status != null) {
            list = list.stream().filter(b -> b.getStatus() == status).collect(Collectors.toList());
        }
        if (startDate != null) {
            list = list.stream().filter(b -> b.getCheckInDate() != null && !b.getCheckInDate().isBefore(startDate)).collect(Collectors.toList());
        }
        if (endDate != null) {
            list = list.stream().filter(b -> b.getCheckOutDate() != null && !b.getCheckOutDate().isAfter(endDate)).collect(Collectors.toList());
        }

        list.sort((a, b) -> Long.compare(b.getId() != null ? b.getId() : 0, a.getId() != null ? a.getId() : 0));

        model.addAttribute("bookings", list);
        model.addAttribute("statuses", BookingStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "booking/list";
    }

    @GetMapping("/search")
    public String searchForm(
            @RequestParam(name = "checkInDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkInDate,
            @RequestParam(name = "checkOutDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkOutDate,
            @RequestParam(name = "roomTypeId", required = false) Long roomTypeId,
            @RequestParam(name = "numGuests", required = false) Integer numGuests,
            Model model) {

        if (checkInDate == null) checkInDate = LocalDate.now();
        if (checkOutDate == null) checkOutDate = checkInDate.plusDays(1);

        RoomSearchDTO searchDTO = RoomSearchDTO.builder()
                .checkInDate(checkInDate)
                .checkOutDate(checkOutDate)
                .roomTypeId(roomTypeId)
                .numGuests(numGuests)
                .build();

        List<Room> availableRooms = bookingService.searchAvailableRooms(searchDTO);

        model.addAttribute("searchDTO", searchDTO);
        model.addAttribute("availableRooms", availableRooms);
        model.addAttribute("roomTypes", roomTypeService.getAllRoomTypes());
        return "booking/search";
    }

    @GetMapping("/new")
    public String createForm(
            @RequestParam(name = "roomIds", required = false) List<Long> roomIds,
            @RequestParam(name = "checkInDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkInDate,
            @RequestParam(name = "checkOutDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkOutDate,
            Model model) {

        if (checkInDate == null) checkInDate = LocalDate.now();
        if (checkOutDate == null) checkOutDate = checkInDate.plusDays(1);

        List<Room> selectedRooms = new ArrayList<>();
        if (roomIds != null) {
            for (Long rid : roomIds) {
                try {
                    selectedRooms.add(roomService.getById(rid));
                } catch (Exception ignored) {}
            }
        }

        BookingCreateDTO dto = BookingCreateDTO.builder()
                .checkInDate(checkInDate)
                .checkOutDate(checkOutDate)
                .roomIds(roomIds)
                .channel(BookingChannel.WALK_IN)
                .numGuests(2)
                .build();

        model.addAttribute("bookingDTO", dto);
        model.addAttribute("selectedRooms", selectedRooms);
        model.addAttribute("allRooms", roomService.getAllRooms());
        model.addAttribute("guests", guestService.getAllGuests());
        model.addAttribute("channels", BookingChannel.values());
        return "booking/form";
    }

    @PostMapping
    public String createBooking(
            @ModelAttribute("bookingDTO") BookingCreateDTO dto,
            @RequestParam(name = "newGuestFullName", required = false) String newGuestFullName,
            @RequestParam(name = "newGuestPhone", required = false) String newGuestPhone,
            @RequestParam(name = "newGuestIdNumber", required = false) String newGuestIdNumber,
            RedirectAttributes ra) {

        try {
            // If new guest details are entered instead of selecting existing guest
            if (dto.getGuestId() == null && newGuestFullName != null && !newGuestFullName.trim().isEmpty()) {
                Guest guest = Guest.builder()
                        .fullName(newGuestFullName.trim())
                        .phone(newGuestPhone)
                        .idNumber(newGuestIdNumber)
                        .nationality("Việt Nam")
                        .build();
                guest = guestService.save(guest);
                dto.setGuestId(guest.getId());
            }

            Booking booking = bookingService.createBooking(dto);
            ra.addFlashAttribute("successMessage", "Tạo đơn đặt phòng thành công! Mã đơn: " + booking.getBookingCode());
            return "redirect:/bookings/" + booking.getId();
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi tạo đặt phòng: " + e.getMessage());
            return "redirect:/bookings/new?checkInDate=" + dto.getCheckInDate() + "&checkOutDate=" + dto.getCheckOutDate();
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        Booking booking = bookingService.getById(id);
        List<BookingDetail> details = bookingDetailRepository.findByBookingId(id);
        BigDecimal totalPaid = paymentService.getTotalPaidForBooking(id);
        BigDecimal currentTotal = bookingService.calculateTotalCharges(id);

        model.addAttribute("booking", booking);
        model.addAttribute("details", details);
        model.addAttribute("payments", paymentRepository.findByBookingId(id));
        model.addAttribute("totalPaid", totalPaid);
        model.addAttribute("currentTotal", currentTotal);
        model.addAttribute("balanceDue", currentTotal.subtract(totalPaid).max(BigDecimal.ZERO));
        model.addAttribute("services", additionalServiceService.getActiveServices());
        model.addAttribute("serviceUsageRepo", serviceUsageRepository);
        return "booking/detail";
    }

    @PostMapping("/{id}/confirm")
    public String confirm(@PathVariable("id") Long id, RedirectAttributes ra) {
        bookingService.confirmBooking(id);
        ra.addFlashAttribute("successMessage", "Đã xác nhận đơn đặt phòng!");
        return "redirect:/bookings/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancel(
            @PathVariable("id") Long id,
            @RequestParam(name = "reason", required = false) String reason,
            RedirectAttributes ra) {
        bookingService.cancelBooking(id, reason);
        ra.addFlashAttribute("successMessage", "Đã hủy đơn đặt phòng!");
        return "redirect:/bookings/" + id;
    }

    @GetMapping("/{id}/checkin")
    public String checkInPage(@PathVariable("id") Long id, Model model) {
        Booking booking = bookingService.getById(id);
        model.addAttribute("booking", booking);
        model.addAttribute("details", bookingDetailRepository.findByBookingId(id));
        model.addAttribute("checkInDTO", CheckInDTO.builder().bookingId(id).guestIdNumber(booking.getGuest() != null ? booking.getGuest().getIdNumber() : "").build());
        return "booking/checkin";
    }

    @PostMapping("/{id}/checkin")
    public String executeCheckIn(@PathVariable("id") Long id, @ModelAttribute("checkInDTO") CheckInDTO dto, RedirectAttributes ra) {
        try {
            dto.setBookingId(id);
            bookingService.checkIn(dto);
            ra.addFlashAttribute("successMessage", "Check-in thành công! Phòng đã chuyển sang trạng thái Đang ở.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi check-in: " + e.getMessage());
        }
        return "redirect:/bookings/" + id;
    }

    @GetMapping("/{id}/checkout")
    public String checkOutPage(@PathVariable("id") Long id, Model model) {
        Booking booking = bookingService.getById(id);
        List<BookingDetail> details = bookingDetailRepository.findByBookingId(id);
        BigDecimal currentTotal = bookingService.calculateTotalCharges(id);
        BigDecimal totalPaid = paymentService.getTotalPaidForBooking(id);
        BigDecimal balance = currentTotal.subtract(totalPaid).max(BigDecimal.ZERO);

        model.addAttribute("booking", booking);
        model.addAttribute("details", details);
        model.addAttribute("currentTotal", currentTotal);
        model.addAttribute("totalPaid", totalPaid);
        model.addAttribute("balanceDue", balance);
        model.addAttribute("paymentMethods", PaymentMethod.values());
        model.addAttribute("serviceUsageRepo", serviceUsageRepository);
        model.addAttribute("checkOutDTO", CheckOutDTO.builder().bookingId(id).paymentMethod(PaymentMethod.CASH).build());
        return "booking/checkout";
    }

    @PostMapping("/{id}/checkout")
    public String executeCheckOut(@PathVariable("id") Long id, @ModelAttribute("checkOutDTO") CheckOutDTO dto, RedirectAttributes ra) {
        try {
            dto.setBookingId(id);
            bookingService.checkOut(dto);
            ra.addFlashAttribute("successMessage", "Check-out và thanh toán thành công! Phòng đã chuyển sang trạng thái Dọn dẹp.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi check-out: " + e.getMessage());
        }
        return "redirect:/bookings/" + id;
    }

    @PostMapping("/{id}/services")
    public String addService(
            @PathVariable("id") Long id,
            @RequestParam("bookingDetailId") Long bookingDetailId,
            @RequestParam("serviceId") Long serviceId,
            @RequestParam(name = "quantity", defaultValue = "1") int quantity,
            RedirectAttributes ra) {
        try {
            bookingService.addServiceUsage(bookingDetailId, serviceId, quantity);
            ra.addFlashAttribute("successMessage", "Đã thêm dịch vụ vào đơn đặt phòng!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi thêm dịch vụ: " + e.getMessage());
        }
        return "redirect:/bookings/" + id;
    }
}
