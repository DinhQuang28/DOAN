package quanly.com.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import quanly.com.model.Property;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
}
