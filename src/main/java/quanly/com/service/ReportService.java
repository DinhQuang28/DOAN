package quanly.com.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quanly.com.dto.DashboardStatsDTO;
import quanly.com.dto.RevenueReportDTO;
import quanly.com.model.Booking;
import quanly.com.model.BookingDetail;
import quanly.com.model.enums.BookingStatus;
import quanly.com.model.enums.RoomStatus;
import quanly.com.repository.BookingDetailRepository;
import quanly.com.repository.BookingRepository;
import quanly.com.repository.PaymentRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportService {

    @Autowired
    private RoomService roomService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingDetailRepository bookingDetailRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    public DashboardStatsDTO getDashboardStats() {
        int total = (int) roomService.countTotal();
        int available = (int) roomService.countByStatus(RoomStatus.AVAILABLE);
        int occupied = (int) roomService.countByStatus(RoomStatus.OCCUPIED);
        int todayIn = bookingService.getTodayCheckIns().size();
        int todayOut = bookingService.getTodayCheckOuts().size();

        // Monthly revenue: sum of completed payments in current month
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime monthStart = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = currentMonth.atEndOfMonth().atTime(23, 59, 59);

        BigDecimal monthlyRev = paymentRepository.findByPaidAtBetween(monthStart, monthEnd).stream()
                .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double occupancyRate = (total > 0) ? ((double) occupied / total) * 100.0 : 0.0;

        return DashboardStatsDTO.builder()
                .totalRooms(total)
                .availableRooms(available)
                .occupiedRooms(occupied)
                .todayCheckIns(todayIn)
                .todayCheckOuts(todayOut)
                .monthlyRevenue(monthlyRev)
                .occupancyRate(Math.round(occupancyRate * 10.0) / 10.0)
                .build();
    }

    public List<RevenueReportDTO> getRevenueLast7Days() {
        List<RevenueReportDTO> list = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(23, 59, 59);

            BigDecimal dayRev = paymentRepository.findByPaidAtBetween(start, end).stream()
                    .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            long bookingCount = bookingRepository.findByCheckInDateBetween(date, date).size();

            list.add(RevenueReportDTO.builder()
                    .date(date)
                    .revenue(dayRev)
                    .bookingCount((int) bookingCount)
                    .build());
        }
        return list;
    }

    public Map<String, BigDecimal> getRevenueByRoomType() {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        List<BookingDetail> details = bookingDetailRepository.findAll();
        for (BookingDetail bd : details) {
            if (bd.getRoom() != null && bd.getRoom().getRoomType() != null && bd.getRoomPrice() != null) {
                String typeName = bd.getRoom().getRoomType().getName();
                result.put(typeName, result.getOrDefault(typeName, BigDecimal.ZERO).add(bd.getRoomPrice()));
            }
        }
        return result;
    }

    public Map<String, Long> getBookingChannelStats() {
        return bookingRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        b -> b.getChannel() != null ? b.getChannel().name() : "OTHER",
                        Collectors.counting()
                ));
    }
}
