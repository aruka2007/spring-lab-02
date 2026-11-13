package kz.iitu.springlab.security;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

/**
 * Individual Variant 10: Authentication events.
 * Listens for authentication success and failure events and writes an audit row for each.
 */
@Component
public class AuthEventListener {

    private final AuthAuditLogRepository auditLogRepository;

    public AuthEventListener(AuthAuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        String roles = event.getAuthentication().getAuthorities().toString();
        auditLogRepository.save(new AuthAuditLog(username, "LOGIN_SUCCESS", "Authenticated with roles " + roles));
    }

    @EventListener
    public void onAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        String username = event.getAuthentication().getName();
        String reason = event.getException().getClass().getSimpleName() + ": " + event.getException().getMessage();
        auditLogRepository.save(new AuthAuditLog(username, "LOGIN_FAILURE", reason));
    }
}
