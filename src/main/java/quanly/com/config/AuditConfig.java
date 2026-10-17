package quanly.com.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import quanly.com.model.User;
import quanly.com.model.AuditLog;
import quanly.com.repository.AuditLogRepository;

import java.time.LocalDateTime;

@Component
public class AuditConfig {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public void logAction(User user, String action, String entityType, Long entityId, String detail, String ipAddress) {
        AuditLog log = AuditLog.builder()
                .user(user)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .detail(detail)
                .ipAddress(ipAddress)
                .timestamp(LocalDateTime.now())
                .build();
        auditLogRepository.save(log);
    }
}
