package quanly.com.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quanly.com.model.Booking;
import quanly.com.model.Payment;
import quanly.com.model.enums.PaymentMethod;
import quanly.com.model.enums.PaymentStatus;
import quanly.com.repository.BookingRepository;
import quanly.com.repository.PaymentRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    public Payment getById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch thanh toán với ID: " + id));
    }

    public Payment processPayment(Long bookingId, BigDecimal amount, PaymentMethod method, String transactionRef, String notes) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đặt phòng với ID: " + bookingId));

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền thanh toán phải lớn hơn 0");
        }

        if (transactionRef == null || transactionRef.trim().isEmpty()) {
            // Generate clean transaction code: PAY + yyyyMMdd + 4 digits
            String datePart = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
            int randPart = java.util.concurrent.ThreadLocalRandom.current().nextInt(1000, 9999);
            transactionRef = "PAY" + datePart + randPart;
        }

        Payment payment = Payment.builder()
                .booking(booking)
                .amount(amount)
                .method(method != null ? method : PaymentMethod.CASH)
                .status(PaymentStatus.COMPLETED)
                .transactionRef(transactionRef)
                .notes(notes)
                .paidAt(LocalDateTime.now())
                .build();

        return paymentRepository.save(payment);
    }

    public Payment refundPayment(Long paymentId, String reason) {
        Payment payment = getById(paymentId);
        payment.setStatus(PaymentStatus.REFUNDED);
        String currentNotes = payment.getNotes() != null ? payment.getNotes() : "";
        payment.setNotes(currentNotes + (currentNotes.isEmpty() ? "" : " | ") + "[HOÀN TIỀN: " + (reason != null ? reason : "Không rõ lý do") + " lúc " + LocalDateTime.now() + "]");
        return paymentRepository.save(payment);
    }

    public List<Payment> getPaymentsByBooking(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAllByOrderByIdDesc();
    }

    public List<Payment> filterPayments(String keyword, PaymentMethod method, PaymentStatus status, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        List<Payment> list = paymentRepository.findAllByOrderByIdDesc();

        return list.stream().filter(p -> {
            boolean match = true;

            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = keyword.trim().toLowerCase();
                boolean matchRef = p.getTransactionRef() != null && p.getTransactionRef().toLowerCase().contains(kw);
                boolean matchBooking = p.getBooking() != null && p.getBooking().getBookingCode() != null && p.getBooking().getBookingCode().toLowerCase().contains(kw);
                boolean matchGuest = p.getBooking() != null && p.getBooking().getGuest() != null && p.getBooking().getGuest().getFullName() != null && p.getBooking().getGuest().getFullName().toLowerCase().contains(kw);
                boolean matchNotes = p.getNotes() != null && p.getNotes().toLowerCase().contains(kw);
                if (!matchRef && !matchBooking && !matchGuest && !matchNotes) {
                    match = false;
                }
            }

            if (match && method != null) {
                if (p.getMethod() != method) {
                    match = false;
                }
            }

            if (match && status != null) {
                if (p.getStatus() != status) {
                    match = false;
                }
            }

            if (match && startDate != null && p.getPaidAt() != null) {
                if (p.getPaidAt().toLocalDate().isBefore(startDate)) {
                    match = false;
                }
            }

            if (match && endDate != null && p.getPaidAt() != null) {
                if (p.getPaidAt().toLocalDate().isAfter(endDate)) {
                    match = false;
                }
            }

            return match;
        }).collect(java.util.stream.Collectors.toList());
    }

    public BigDecimal getTotalPaidForBooking(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId).stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTodayTotal() {
        java.time.LocalDate today = java.time.LocalDate.now();
        return paymentRepository.findAll().stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED && p.getPaidAt() != null && p.getPaidAt().toLocalDate().equals(today))
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalRevenue() {
        return paymentRepository.findAll().stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
