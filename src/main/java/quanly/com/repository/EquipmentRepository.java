package quanly.com.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import quanly.com.model.Equipment;
import quanly.com.model.enums.EquipmentCategory;
import quanly.com.model.enums.EquipmentStatus;

import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    List<Equipment> findByRoomId(Long roomId);

    List<Equipment> findByRoomIsNull();

    List<Equipment> findByStatus(EquipmentStatus status);

    List<Equipment> findByCategory(EquipmentCategory category);

    long countByStatus(EquipmentStatus status);

    boolean existsByCode(String code);

    @Query("SELECT e FROM Equipment e " +
           "WHERE (:keyword IS NULL OR :keyword = '' OR LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(e.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:status IS NULL OR e.status = :status) " +
           "AND (:category IS NULL OR e.category = :category) " +
           "AND (:roomId IS NULL OR e.room.id = :roomId) " +
           "ORDER BY e.id DESC")
    List<Equipment> search(
            @Param("keyword") String keyword,
            @Param("status") EquipmentStatus status,
            @Param("category") EquipmentCategory category,
            @Param("roomId") Long roomId
    );
}

