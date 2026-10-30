package kz.iitu.springlab;

import kz.iitu.springlab.account.Account;
import kz.iitu.springlab.account.AccountRepository;
import kz.iitu.springlab.account.AccountService;
import kz.iitu.springlab.account.AuditLog;
import kz.iitu.springlab.account.AuditLogRepository;
import org.example.springlab02.SpringLab02Application;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = SpringLab02Application.class)
class LaboratoryWork9Test {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long aliceId;
    private Long bobId;

    @BeforeEach
    void setUp() {
        // Reset test data
        auditLogRepository.deleteAll();
        accountRepository.deleteAll();

        Account alice = accountRepository.save(new Account("Alice", new BigDecimal("1000.00")));
        Account bob = accountRepository.save(new Account("Bob", new BigDecimal("500.00")));

        aliceId = alice.getId();
        bobId = bob.getId();
    }

    @Test
    @DisplayName("Task A: Successful transfer moves funds atomically")
    void testSuccessfulTransfer() {
        accountService.transfer(aliceId, bobId, new BigDecimal("100.00"));

        Account aliceAfter = accountService.findById(aliceId);
        Account bobAfter = accountService.findById(bobId);

        assertThat(aliceAfter.getBalance()).isEqualByComparingTo("900.00");
        assertThat(bobAfter.getBalance()).isEqualByComparingTo("600.00");
    }

    @Test
    @DisplayName("Task A: Insufficient funds throws exception and rolls back both balances")
    void testInsufficientFundsRollback() {
        assertThatThrownBy(() ->
                accountService.transfer(aliceId, bobId, new BigDecimal("2000.00"))
        ).isInstanceOf(IllegalStateException.class)
         .hasMessageContaining("Insufficient funds");

        Account aliceAfter = accountService.findById(aliceId);
        Account bobAfter = accountService.findById(bobId);

        assertThat(aliceAfter.getBalance()).isEqualByComparingTo("1000.00");
        assertThat(bobAfter.getBalance()).isEqualByComparingTo("500.00");
    }

    @Test
    @DisplayName("Task A: Unchecked exception after debit triggers automatic transaction rollback")
    void testUncheckedExceptionRollback() {
        assertThatThrownBy(() ->
                accountService.transferWithUncheckedException(aliceId, bobId, new BigDecimal("100.00"))
        ).isInstanceOf(RuntimeException.class);

        Account aliceAfter = accountService.findById(aliceId);
        Account bobAfter = accountService.findById(bobId);

        assertThat(aliceAfter.getBalance()).isEqualByComparingTo("1000.00");
        assertThat(bobAfter.getBalance()).isEqualByComparingTo("500.00");
    }

    @Test
    @DisplayName("Task A: Checked exception with rollbackFor rolls back transaction")
    void testCheckedExceptionWithRollbackFor() {
        assertThatThrownBy(() ->
                accountService.transferWithCheckedRollback(aliceId, bobId, new BigDecimal("100.00"))
        ).isInstanceOf(Exception.class);

        Account aliceAfter = accountService.findById(aliceId);
        assertThat(aliceAfter.getBalance()).isEqualByComparingTo("1000.00");
    }

    @Test
    @DisplayName("Task B: Propagation.REQUIRED rolls back audit record when outer transaction fails")
    void testPropagationRequiredRollback() {
        assertThatThrownBy(() ->
                accountService.transferWithAuditRequired(aliceId, bobId, new BigDecimal("100.00"), true)
        ).isInstanceOf(RuntimeException.class);

        List<AuditLog> logs = auditLogRepository.findAll();
        assertThat(logs).isEmpty(); // Both transfer and audit rolled back together!
    }

    @Test
    @DisplayName("Task B: Propagation.REQUIRES_NEW commits audit record even when outer transaction rolls back")
    void testPropagationRequiresNewCommitted() {
        assertThatThrownBy(() ->
                accountService.transferWithAuditRequiresNew(aliceId, bobId, new BigDecimal("100.00"), true)
        ).isInstanceOf(RuntimeException.class);

        // Account transfer was rolled back:
        Account aliceAfter = accountService.findById(aliceId);
        assertThat(aliceAfter.getBalance()).isEqualByComparingTo("1000.00");

        // BUT audit log persisted because of REQUIRES_NEW!
        List<AuditLog> logs = auditLogRepository.findAll();
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getAction()).isEqualTo("TRANSFER_REQUIRES_NEW");
        assertThat(logs.get(0).getAccountId()).isEqualTo(aliceId);
    }

    @Test
    @DisplayName("Task C: Isolation levels READ_COMMITTED and REPEATABLE_READ query accurately")
    void testIsolationLevels() {
        BigDecimal rc = accountService.getBalanceReadCommitted(aliceId);
        BigDecimal rr = accountService.getBalanceRepeatableRead(aliceId);

        assertThat(rc).isEqualByComparingTo("1000.00");
        assertThat(rr).isEqualByComparingTo("1000.00");
    }

    @Test
    @DisplayName("Task D: Optimistic locking detects concurrent update conflict via @Version")
    void testOptimisticLockingConflict() {
        Account alice1 = accountService.findById(aliceId);
        Account staleAlice = new Account(alice1.getId(), alice1.getOwnerName(), alice1.getBalance(), alice1.getVersion(), alice1.getStatus());

        // First update succeeds and increments version
        accountService.updateBalance(aliceId, new BigDecimal("1200.00"));

        Account aliceUpdated = accountService.findById(aliceId);
        assertThat(aliceUpdated.getVersion()).isGreaterThan(staleAlice.getVersion());

        // Attempting to save stale entity with old version fails
        assertThatThrownBy(() ->
                accountService.saveStaleAccount(staleAlice)
        ).isInstanceOf(OptimisticLockingFailureException.class);
    }

    @Test
    @DisplayName("Task E: Flyway applied V1, V2, and V3 migrations successfully")
    void testFlywayMigrationsApplied() {
        MigrationInfo[] applied = flyway.info().applied();
        assertThat(applied).hasSizeGreaterThanOrEqualTo(3);

        assertThat(applied[0].getVersion().getVersion()).isEqualTo("1");
        assertThat(applied[0].getDescription()).isEqualTo("create accounts");

        assertThat(applied[1].getVersion().getVersion()).isEqualTo("2");
        assertThat(applied[1].getDescription()).isEqualTo("add status");

        assertThat(applied[2].getVersion().getVersion()).isEqualTo("3");
        assertThat(applied[2].getDescription()).isEqualTo("add audit and indexes");

        // Also verify flyway_schema_history table via JDBC
        Integer historyRows = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM \"flyway_schema_history\" WHERE \"success\" = TRUE",
                Integer.class
        );
        assertThat(historyRows).isGreaterThanOrEqualTo(3);
    }
}
