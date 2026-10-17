package quanly.com.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import quanly.com.model.*;
import quanly.com.model.enums.RoomStatus;
import quanly.com.model.enums.UserRole;
import quanly.com.repository.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ServiceEntityRepository serviceEntityRepository;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingDetailRepository bookingDetailRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            System.out.println("Initializing seed data for Sunshine Homestay...");

            Property property = Property.builder()
                    .name("Sunshine Homestay")
                    .address("123 Nguyen Hue, Da Nang")
                    .phone("0905123456")
                    .email("contact@sunshinehomestay.vn")
                    .description("Homestay tiện nghi, thân thiện tại trung tâm Đà Nẵng")
                    .build();
            property = propertyRepository.save(property);

            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("Quản trị viên")
                    .email("admin@sunshinehomestay.vn")
                    .phone("0905111222")
                    .role(UserRole.ROLE_ADMIN)
                    .property(property)
                    .enabled(true)
                    .build();
            userRepository.save(admin);

            User manager = User.builder()
                    .username("manager")
                    .password(passwordEncoder.encode("manager123"))
                    .fullName("Quản lý cơ sở")
                    .email("manager@sunshinehomestay.vn")
                    .phone("0905333444")
                    .role(UserRole.ROLE_MANAGER)
                    .property(property)
                    .enabled(true)
                    .build();
            userRepository.save(manager);

            User receptionist = User.builder()
                    .username("letan01")
                    .password(passwordEncoder.encode("letan123"))
                    .fullName("Lễ tân Ca sáng")
                    .email("letan@sunshinehomestay.vn")
                    .phone("0905555666")
                    .role(UserRole.ROLE_RECEPTIONIST)
                    .property(property)
                    .enabled(true)
                    .build();
            userRepository.save(receptionist);

            RoomType singleRoom = RoomType.builder()
                    .name("Phòng Đơn")
                    .description("Phòng 1 giường đơn tiêu chuẩn, thích hợp đi công tác hoặc du lịch 1 mình")
                    .maxGuests(1)
                    .basePrice(new BigDecimal("300000"))
                    .amenities("WiFi miễn phí, TV màn hình phẳng, Điều hòa 2 chiều, Bàn làm việc, Máy sấy tóc")
                    .build();
            singleRoom = roomTypeRepository.save(singleRoom);

            RoomType doubleRoom = RoomType.builder()
                    .name("Phòng Đôi")
                    .description("Phòng 1 giường đôi cỡ lớn, phù hợp cho cặp đôi")
                    .maxGuests(2)
                    .basePrice(new BigDecimal("500000"))
                    .amenities("WiFi miễn phí, TV truyền hình cáp, Điều hòa, Tủ lạnh mini, Ấm đun nước, Ban công")
                    .build();
            doubleRoom = roomTypeRepository.save(doubleRoom);

            RoomType familyRoom = RoomType.builder()
                    .name("Phòng Gia Đình")
                    .description("Phòng rộng rãi 2 giường lớn cho gia đình 4 người")
                    .maxGuests(4)
                    .basePrice(new BigDecimal("800000"))
                    .amenities("WiFi, Smart TV, Điều hòa, Tủ lạnh lớn, Bếp mini nấu nướng, Bàn ăn, Sofa")
                    .build();
            familyRoom = roomTypeRepository.save(familyRoom);

            RoomType vipRoom = RoomType.builder()
                    .name("Phòng VIP Suite")
                    .description("Phòng cao cấp hướng biển view toàn cảnh, bồn tắm nằm sang trọng")
                    .maxGuests(2)
                    .basePrice(new BigDecimal("1200000"))
                    .amenities("WiFi tốc độ cao, Smart TV 55 inch, Điều hòa, Minibar cao cấp, Bồn tắm nằm, Ban công panorama")
                    .build();
            vipRoom = roomTypeRepository.save(vipRoom);

            List<Room> rooms = new ArrayList<>();
            for (int floor = 1; floor <= 4; floor++) {
                for (int num = 1; num <= 5; num++) {
                    RoomType rt;
                    if (floor == 1) {
                        rt = (num <= 3) ? singleRoom : doubleRoom;
                    } else if (floor == 2) {
                        rt = (num <= 3) ? doubleRoom : familyRoom;
                    } else if (floor == 3) {
                        rt = (num <= 3) ? familyRoom : vipRoom;
                    } else {
                        rt = vipRoom;
                    }

                    Room room = Room.builder()
                            .roomNumber(String.valueOf(floor * 100 + num))
                            .floor(floor)
                            .status(RoomStatus.AVAILABLE)
                            .roomType(rt)
                            .property(property)
                            .notes("Phòng tầng " + floor + " hướng thoáng")
                            .build();
                    rooms.add(room);
                }
            }
            roomRepository.saveAll(rooms);

            saveService("Ăn sáng buffet", "80000", "Ăn uống", "Bữa sáng đầy đủ dinh dưỡng món Á - Âu");
            saveService("Giặt ủi tiêu chuẩn", "50000", "Giặt ủi", "Giặt sấy lấy liền trong ngày");
            saveService("Đưa đón sân bay Đà Nẵng", "300000", "Vận chuyển", "Xe 4 chỗ đưa đón sân bay tận nơi");
            saveService("Thuê xe máy tự lái (xe ga)", "150000", "Vận chuyển", "Xe tay ga đời mới kèm 2 nón bảo hiểm");
            saveService("Massage thư giãn body 60p", "200000", "Spa", "Massage tinh dầu thảo dược thiên nhiên");

            Promotion promo = Promotion.builder()
                    .code("WELCOME10")
                    .name("Chào mừng khách mới")
                    .description("Giảm giá 10% cho khách đặt phòng lần đầu")
                    .type("PERCENTAGE")
                    .value(new BigDecimal("10"))
                    .maxUses(100)
                    .currentUses(0)
                    .active(true)
                    .build();
            promotionRepository.save(promo);

            // Seed Sample Guests
            Guest guest1 = Guest.builder()
                    .fullName("Nguyễn Văn An")
                    .phone("0905123456")
                    .email("nguyenvanan@gmail.com")
                    .idNumber("048195001234")
                    .nationality("Việt Nam")
                    .loyaltyPoints(15)
                    .build();
            guest1 = guestRepository.save(guest1);

            Guest guest2 = Guest.builder()
                    .fullName("Trần Thị Mai")
                    .phone("0987654321")
                    .email("tranmai@gmail.com")
                    .idNumber("048198005678")
                    .nationality("Việt Nam")
                    .loyaltyPoints(5)
                    .build();
            guest2 = guestRepository.save(guest2);

            // Seed Sample Bookings
            if (!rooms.isEmpty()) {
                Room r1 = rooms.get(0); // 101
                r1.setStatus(RoomStatus.OCCUPIED);
                roomRepository.save(r1);

                Booking booking1 = Booking.builder()
                        .bookingCode("BK20261001")
                        .guest(guest1)
                        .checkInDate(java.time.LocalDate.now().minusDays(1))
                        .checkOutDate(java.time.LocalDate.now().plusDays(2))
                        .status(quanly.com.model.enums.BookingStatus.CHECKED_IN)
                        .channel(quanly.com.model.enums.BookingChannel.BOOKING_COM)
                        .totalAmount(new BigDecimal("900000"))
                        .discount(BigDecimal.ZERO)
                        .notes("Khách đặt qua OTA, yêu cầu phòng tầng thấp yên tĩnh")
                        .actualCheckIn(java.time.LocalDateTime.now().minusDays(1).withHour(14).withMinute(0))
                        .build();
                booking1 = bookingRepository.save(booking1);

                BookingDetail detail1 = BookingDetail.builder()
                        .booking(booking1)
                        .room(r1)
                        .roomPrice(new BigDecimal("900000"))
                        .numGuests(2)
                        .build();
                bookingDetailRepository.save(detail1);

                // Sample Payment for Booking 1
                Payment pay1 = Payment.builder()
                        .booking(booking1)
                        .amount(new BigDecimal("500000"))
                        .method(quanly.com.model.enums.PaymentMethod.BANK_TRANSFER)
                        .status(quanly.com.model.enums.PaymentStatus.COMPLETED)
                        .transactionRef("PAY20261017001")
                        .notes("Khách đặt cọc 500k qua VietQR MB Bank")
                        .paidAt(java.time.LocalDateTime.now().minusDays(1).withHour(14).withMinute(15))
                        .build();
                paymentRepository.save(pay1);

                Room r2 = rooms.get(1); // 102
                Booking booking2 = Booking.builder()
                        .bookingCode("BK20261002")
                        .guest(guest2)
                        .checkInDate(java.time.LocalDate.now())
                        .checkOutDate(java.time.LocalDate.now().plusDays(1))
                        .status(quanly.com.model.enums.BookingStatus.CONFIRMED)
                        .channel(quanly.com.model.enums.BookingChannel.WALK_IN)
                        .totalAmount(new BigDecimal("300000"))
                        .discount(BigDecimal.ZERO)
                        .notes("Khách vãng lai đến quầy")
                        .build();
                booking2 = bookingRepository.save(booking2);

                BookingDetail detail2 = BookingDetail.builder()
                        .booking(booking2)
                        .room(r2)
                        .roomPrice(new BigDecimal("300000"))
                        .numGuests(1)
                        .build();
                bookingDetailRepository.save(detail2);

                Payment pay2 = Payment.builder()
                        .booking(booking2)
                        .amount(new BigDecimal("300000"))
                        .method(quanly.com.model.enums.PaymentMethod.CASH)
                        .status(quanly.com.model.enums.PaymentStatus.COMPLETED)
                        .transactionRef("PAY20261017002")
                        .notes("Thu đủ 100% tiền mặt tại quầy lễ tân")
                        .paidAt(java.time.LocalDateTime.now().withHour(10).withMinute(30))
                        .build();
                paymentRepository.save(pay2);
            }

            System.out.println("Data initialization with payments for Sunshine Homestay completed successfully!");
        }
    }

    private void saveService(String name, String price, String category, String description) {
        ServiceEntity service = ServiceEntity.builder()
                .name(name)
                .price(new BigDecimal(price))
                .category(category)
                .description(description)
                .active(true)
                .build();
        serviceEntityRepository.save(service);
    }
}
