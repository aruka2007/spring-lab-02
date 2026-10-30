package kz.iitu.springlab.account;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public AuditLog logRequired(String action, Long accountId, BigDecimal amount) {
        return auditLogRepository.save(new AuditLog(action, accountId, amount));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLog logRequiresNew(String action, Long accountId, BigDecimal amount) {
        return auditLogRepository.save(new AuditLog(action, accountId, amount));
    }
}
