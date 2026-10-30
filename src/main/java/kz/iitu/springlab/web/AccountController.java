package kz.iitu.springlab.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.iitu.springlab.account.Account;
import kz.iitu.springlab.account.AccountRepository;
import kz.iitu.springlab.account.AccountService;
import kz.iitu.springlab.account.AuditLog;
import kz.iitu.springlab.account.AuditLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Account & Transactions API", description = "Laboratory Work 9 Transaction Management & Database Versioning")
public class AccountController {

    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final AuditLogRepository auditLogRepository;

    public AccountController(AccountService accountService,
                             AccountRepository accountRepository,
                             AuditLogRepository auditLogRepository) {
        this.accountService = accountService;
        this.accountRepository = accountRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    @Operation(summary = "List all accounts", description = "Shows Alice and Bob accounts with balance, status, and version")
    public List<Account> getAllAccounts() {
        return accountService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get account by ID")
    public Account getAccount(@PathVariable Long id) {
        return accountService.findById(id);
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "List audit log records", description = "Shows persisted audit records after propagation experiments")
    public List<AuditLog> getAuditLogs() {
        return auditLogRepository.findAll();
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transfer funds between accounts (Task A)")
    public ResponseEntity<?> transfer(@RequestParam Long fromId,
                                      @RequestParam Long toId,
                                      @RequestParam BigDecimal amount) {
        accountService.transfer(fromId, toId, amount);
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Transferred " + amount + " from account " + fromId + " to account " + toId,
                "accounts", accountService.findAll()
        ));
    }

    @PostMapping("/transfer-with-propagation")
    @Operation(summary = "Transfer with audit propagation (Task B: REQUIRED vs REQUIRES_NEW)")
    public ResponseEntity<?> transferWithPropagation(@RequestParam Long fromId,
                                                     @RequestParam Long toId,
                                                     @RequestParam BigDecimal amount,
                                                     @RequestParam String propagation,
                                                     @RequestParam boolean failAfterAudit) {
        try {
            if ("REQUIRES_NEW".equalsIgnoreCase(propagation)) {
                accountService.transferWithAuditRequiresNew(fromId, toId, amount, failAfterAudit);
            } else {
                accountService.transferWithAuditRequired(fromId, toId, amount, failAfterAudit);
            }
            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "Operation completed without failure",
                    "accounts", accountService.findAll()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "ROLLED_BACK",
                    "error", e.getMessage(),
                    "auditLogsCount", auditLogRepository.count(),
                    "auditLogs", auditLogRepository.findAll()
            ));
        }
    }
}
