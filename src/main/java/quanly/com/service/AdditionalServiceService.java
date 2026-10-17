package quanly.com.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quanly.com.model.ServiceEntity;
import quanly.com.repository.ServiceEntityRepository;

import java.util.List;

@Service
@Transactional
public class AdditionalServiceService {

    @Autowired
    private ServiceEntityRepository serviceEntityRepository;

    public List<ServiceEntity> getAllServices() {
        return serviceEntityRepository.findAll();
    }

    public List<ServiceEntity> getActiveServices() {
        return serviceEntityRepository.findByActiveTrue();
    }

    public List<ServiceEntity> getByCategory(String category) {
        return serviceEntityRepository.findByCategory(category);
    }

    public ServiceEntity getById(Long id) {
        return serviceEntityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dịch vụ với ID: " + id));
    }

    public ServiceEntity save(ServiceEntity service) {
        return serviceEntityRepository.save(service);
    }

    public ServiceEntity update(Long id, ServiceEntity updated) {
        ServiceEntity existing = getById(id);
        existing.setName(updated.getName());
        existing.setPrice(updated.getPrice());
        existing.setCategory(updated.getCategory());
        existing.setDescription(updated.getDescription());
        existing.setActive(updated.isActive());
        return serviceEntityRepository.save(existing);
    }

    public void delete(Long id) {
        serviceEntityRepository.deleteById(id);
    }
}
