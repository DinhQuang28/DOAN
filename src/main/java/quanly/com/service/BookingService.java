package quanly.com.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quanly.com.dto.BookingCreateDTO;
import quanly.com.dto.CheckInDTO;
import quanly.com.dto.CheckOutDTO;
import quanly.com.dto.RoomSearchDTO;
import quanly.com.model.*;
import quanly.com.model.enums.*;
import quanly.com.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@Transactional
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingDetailRepository bookingDetailRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private ServiceEntityRepository serviceEntityRepository;

    @Autowired
    private ServiceUsageRepository serviceUsageRepository;

    @Autowired
    private RoomService roomService;

    @Autowired
    private PriceSeasonService priceSeasonService;

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private PaymentService paymentService;

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Booking getById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn đặt phòng với ID: " + id));
    }

    public Optional<Booking> getByCode(String code) {
        return bookingRepository.findByBookingCode(code);
    }

    public List<Booking> getBookingsByStatus(BookingStatus status) {
        return bookingRepository.findByStatus(status);
    }

    public List<Booking> getRecentBookings(int limit) {
        return bookingRepository.findAll().stream()
                .sorted((a, b) -> {
                    LocalDateTime ta = (a.getCreatedAt() != null) ? a.getCreatedAt() : LocalDateTime.MIN;
                    LocalDateTime tb = (b.getCreatedAt() != null) ? b.getCreatedAt() : LocalDateTime.MIN;
                    return tb.compareTo(ta);
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<Room> searchAvailableRooms(RoomSearchDTO dto) {
        LocalDate checkIn = (dto.getCheckInDate() != null) ? dto.getCheckInDate() : LocalDate.now();
        LocalDate checkOut = (dto.getCheckOutDate() != null) ? dto.getCheckOutDate() : checkIn.plusDays(1);
        return roomService.getAvailableRooms(checkIn, checkOut, dto.getRoomTypeId(), dto.getNumGuests());
    }

    public Booking createBooking(BookingCreateDTO dto) {
        if (dto.getCheckInDate() == null || dto.getCheckOutDate() == null) {
            throw new IllegalArgumentException("Ngày nhận phòng và trả phòng không được để trống");
        }
        if (!dto.getCheckOutDate().isAfter(dto.getCheckInDate())) {
            throw new IllegalArgumentException("Ngày trả phòng phải sau ngày nhận phòng");
        }
        if (dto.getRoomIds() == null || dto.getRoomIds().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất một phòng");
        }

        Guest guest = guestRepository.findById(dto.getGuestId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin khách hàng"));

        // Generate unique booking code: BK + yyyyMMdd + 4 digits
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int randPart = ThreadLocalRandom.current().nextInt(1000, 9999);
        String code = "BK" + datePart + randPart;

        // Calculate prices
        BigDecimal subtotal = BigDecimal.ZERO;
        List<Room> selectedRooms = new ArrayList<>();
        Map<Room, BigDecimal> roomPrices = new HashMap<>();

        for (Long roomId : dto.getRoomIds()) {
            Room r = roomRepository.findById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("Phòng ID " + roomId + " không tồn tại"));
            selectedRooms.add(r);

            BigDecimal roomPrice = priceSeasonService.calculateTotalPrice(
                    r.getRoomType().getId(), dto.getCheckInDate(), dto.getCheckOutDate());
            roomPrices.put(r, roomPrice);
            subtotal = subtotal.add(roomPrice);
        }

        // Apply discount if promotion code provided
        BigDecimal discount = BigDecimal.ZERO;
        Promotion promo = null;
        if (dto.getPromotionCode() != null && !dto.getPromotionCode().trim().isEmpty()) {
            discount = promotionService.calculateDiscount(dto.getPromotionCode(), subtotal);
            promo = promotionService.findByCode(dto.getPromotionCode()).orElse(null);
            if (promo != null) {
                promotionService.incrementUsage(dto.getPromotionCode());
            }
        }

        BigDecimal totalAmount = subtotal.subtract(discount).max(BigDecimal.ZERO);

        Booking booking = Booking.builder()
                .bookingCode(code)
                .guest(guest)
                .checkInDate(dto.getCheckInDate())
                .checkOutDate(dto.getCheckOutDate())
                .status(BookingStatus.CONFIRMED)
                .channel(dto.getChannel() != null ? dto.getChannel() : BookingChannel.WALK_IN)
                .totalAmount(totalAmount)
                .discount(discount)
                .promotion(promo)
                .notes(dto.getNotes())
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        // Save BookingDetails
        for (Room r : selectedRooms) {
            BookingDetail detail = BookingDetail.builder()
                    .booking(savedBooking)
                    .room(r)
                    .roomPrice(roomPrices.get(r))
                    .numGuests(dto.getNumGuests() > 0 ? dto.getNumGuests() : (r.getRoomType() != null ? r.getRoomType().getMaxGuests() : 1))
                    .build();
            bookingDetailRepository.save(detail);
        }

        return savedBooking;
    }

    public void confirmBooking(Long bookingId) {
        Booking booking = getById(bookingId);
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
    }

    public void cancelBooking(Long bookingId, String reason) {
        Booking booking = getById(bookingId);
        booking.setStatus(BookingStatus.CANCELLED);
        if (reason != null && !reason.trim().isEmpty()) {
            booking.setNotes((booking.getNotes() != null ? booking.getNotes() + " | " : "") + "Lý do hủy: " + reason);
        }
        bookingRepository.save(booking);

        // If rooms were occupied, revert them to available
        List<BookingDetail> details = bookingDetailRepository.findByBookingId(bookingId);
        for (BookingDetail bd : details) {
            if (bd.getRoom() != null && bd.getRoom().getStatus() == RoomStatus.OCCUPIED) {
                roomService.updateRoomStatus(bd.getRoom().getId(), RoomStatus.AVAILABLE);
            }
        }
    }

    public void checkIn(CheckInDTO dto) {
        Booking booking = getById(dto.getBookingId());
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Đơn đặt phòng đã bị hủy, không thể check-in");
        }

        booking.setStatus(BookingStatus.CHECKED_IN);
        booking.setActualCheckIn(LocalDateTime.now());
        if (dto.getNotes() != null && !dto.getNotes().trim().isEmpty()) {
            booking.setNotes((booking.getNotes() != null ? booking.getNotes() + " | " : "") + dto.getNotes());
        }
        bookingRepository.save(booking);

        // Update guest ID number if provided
        if (dto.getGuestIdNumber() != null && !dto.getGuestIdNumber().trim().isEmpty()) {
            Guest guest = booking.getGuest();
            if (guest != null) {
                guest.setIdNumber(dto.getGuestIdNumber().trim());
                guestRepository.save(guest);
            }
        }

        // Set rooms to OCCUPIED
        List<BookingDetail> details = bookingDetailRepository.findByBookingId(booking.getId());
        for (BookingDetail bd : details) {
            if (bd.getRoom() != null) {
                roomService.updateRoomStatus(bd.getRoom().getId(), RoomStatus.OCCUPIED);
            }
        }
    }

    public void checkOut(CheckOutDTO dto) {
        Booking booking = getById(dto.getBookingId());
        if (booking.getStatus() != BookingStatus.CHECKED_IN) {
            throw new IllegalStateException("Đơn đặt phòng chưa check-in hoặc đã hoàn tất");
        }

        booking.setStatus(BookingStatus.CHECKED_OUT);
        booking.setActualCheckOut(LocalDateTime.now());
        bookingRepository.save(booking);

        // Process payment if method provided
        if (dto.getPaymentMethod() != null) {
            BigDecimal totalCharge = calculateTotalCharges(booking.getId());
            BigDecimal alreadyPaid = paymentService.getTotalPaidForBooking(booking.getId());
            BigDecimal balance = totalCharge.subtract(alreadyPaid);
            if (balance.compareTo(BigDecimal.ZERO) > 0) {
                paymentService.processPayment(booking.getId(), balance, dto.getPaymentMethod(), null, dto.getNotes());
            }
        }

        // Set rooms to CLEANING
        List<BookingDetail> details = bookingDetailRepository.findByBookingId(booking.getId());
        for (BookingDetail bd : details) {
            if (bd.getRoom() != null) {
                roomService.updateRoomStatus(bd.getRoom().getId(), RoomStatus.CLEANING);
            }
        }

        // Award loyalty points: 1 pt per 100,000 VND
        if (booking.getGuest() != null && booking.getTotalAmount() != null) {
            int points = booking.getTotalAmount().divide(new BigDecimal("100000"), 0, java.math.RoundingMode.FLOOR).intValue();
            if (points > 0) {
                guestRepository.findById(booking.getGuest().getId()).ifPresent(g -> {
                    g.setLoyaltyPoints(g.getLoyaltyPoints() + points);
                    guestRepository.save(g);
                });
            }
        }
    }

    public ServiceUsage addServiceUsage(Long bookingDetailId, Long serviceId, int quantity) {
        BookingDetail bd = bookingDetailRepository.findById(bookingDetailId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi tiết đặt phòng"));
        ServiceEntity svc = serviceEntityRepository.findById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dịch vụ"));

        ServiceUsage usage = ServiceUsage.builder()
                .bookingDetail(bd)
                .serviceEntity(svc)
                .quantity(quantity > 0 ? quantity : 1)
                .unitPrice(svc.getPrice())
                .usedAt(LocalDateTime.now())
                .build();

        ServiceUsage saved = serviceUsageRepository.save(usage);

        // Recalculate and update booking totalAmount
        Booking booking = bd.getBooking();
        BigDecimal newTotal = calculateTotalCharges(booking.getId());
        booking.setTotalAmount(newTotal);
        bookingRepository.save(booking);

        return saved;
    }

    public BigDecimal calculateTotalCharges(Long bookingId) {
        Booking booking = getById(bookingId);
        List<BookingDetail> details = bookingDetailRepository.findByBookingId(bookingId);

        BigDecimal roomCharges = BigDecimal.ZERO;
        BigDecimal serviceCharges = BigDecimal.ZERO;

        for (BookingDetail bd : details) {
            if (bd.getRoomPrice() != null) {
                roomCharges = roomCharges.add(bd.getRoomPrice());
            }
            List<ServiceUsage> usages = serviceUsageRepository.findByBookingDetailId(bd.getId());
            for (ServiceUsage u : usages) {
                if (u.getUnitPrice() != null) {
                    serviceCharges = serviceCharges.add(u.getUnitPrice().multiply(new BigDecimal(u.getQuantity())));
                }
            }
        }

        BigDecimal discount = (booking.getDiscount() != null) ? booking.getDiscount() : BigDecimal.ZERO;
        return roomCharges.add(serviceCharges).subtract(discount).max(BigDecimal.ZERO);
    }

    public List<Booking> getTodayCheckIns() {
        LocalDate today = LocalDate.now();
        return bookingRepository.findAll().stream()
                .filter(b -> today.equals(b.getCheckInDate()) && (b.getStatus() == BookingStatus.CONFIRMED || b.getStatus() == BookingStatus.PENDING))
                .collect(Collectors.toList());
    }

    public List<Booking> getTodayCheckOuts() {
        LocalDate today = LocalDate.now();
        return bookingRepository.findAll().stream()
                .filter(b -> today.equals(b.getCheckOutDate()) && b.getStatus() == BookingStatus.CHECKED_IN)
                .collect(Collectors.toList());
    }
}
