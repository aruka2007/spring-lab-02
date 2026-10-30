package kz.iitu.springlab.account;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AuditService auditService;

    public AccountService(AccountRepository accountRepository, AuditService auditService) {
        this.accountRepository = accountRepository;
        this.auditService = auditService;
    }

    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    public Account findById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));
    }

    // Task A: Standard declarative transfer with automatic rollback on unchecked exceptions
    @Transactional
    public void transfer(Long fromId, Long toId, BigDecimal amount) {
        Account from = findById(fromId);
        Account to = findById(toId);

        from.debit(amount);
        to.credit(amount);

        accountRepository.save(from);
        accountRepository.save(to);
    }

    // Task A: Transfer with unexpected runtime exception after debit to demonstrate rollback
    @Transactional
    public void transferWithUncheckedException(Long fromId, Long toId, BigDecimal amount) {
        Account from = findById(fromId);
        Account to = findById(toId);

        from.debit(amount);
        accountRepository.save(from);

        if (true) {
            throw new RuntimeException("Simulated unexpected runtime exception before credit!");
        }

        to.credit(amount);
        accountRepository.save(to);
    }

    // Task A: Checked exception without rollbackFor (Spring default: commits transaction!)
    @Transactional
    public void transferWithCheckedNoRollback(Long fromId, Long toId, BigDecimal amount) throws Exception {
        Account from = findById(fromId);
        from.debit(amount);
        accountRepository.save(from);

        throw new Exception("Checked exception without rollbackFor");
    }

    // Task A: Checked exception with rollbackFor = Exception.class (Spring rolls back!)
    @Transactional(rollbackFor = Exception.class)
    public void transferWithCheckedRollback(Long fromId, Long toId, BigDecimal amount) throws Exception {
        Account from = findById(fromId);
        from.debit(amount);
        accountRepository.save(from);

        throw new Exception("Checked exception with rollbackFor");
    }

    // Task B: Propagation.REQUIRED experiment
    @Transactional(propagation = Propagation.REQUIRED)
    public void transferWithAuditRequired(Long fromId, Long toId, BigDecimal amount, boolean failAfterAudit) {
        Account from = findById(fromId);
        Account to = findById(toId);

        from.debit(amount);
        to.credit(amount);
        accountRepository.save(from);
        accountRepository.save(to);

        auditService.logRequired("TRANSFER_REQUIRED", fromId, amount);

        if (failAfterAudit) {
            throw new RuntimeException("Forced failure after audit with REQUIRED propagation");
        }
    }

    // Task B: Propagation.REQUIRES_NEW experiment
    @Transactional(propagation = Propagation.REQUIRED)
    public void transferWithAuditRequiresNew(Long fromId, Long toId, BigDecimal amount, boolean failAfterAudit) {
        Account from = findById(fromId);
        Account to = findById(toId);

        from.debit(amount);
        to.credit(amount);
        accountRepository.save(from);
        accountRepository.save(to);

        auditService.logRequiresNew("TRANSFER_REQUIRES_NEW", fromId, amount);

        if (failAfterAudit) {
            throw new RuntimeException("Forced failure after audit with REQUIRES_NEW propagation");
        }
    }

    // Task C: Isolation READ_COMMITTED
    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    public BigDecimal getBalanceReadCommitted(Long id) {
        return findById(id).getBalance();
    }

    // Task C: Isolation REPEATABLE_READ
    @Transactional(isolation = Isolation.REPEATABLE_READ, readOnly = true)
    public BigDecimal getBalanceRepeatableRead(Long id) {
        return findById(id).getBalance();
    }

    // Task D: Optimistic locking update
    @Transactional
    public Account updateBalance(Long id, BigDecimal newBalance) {
        Account account = findById(id);
        account.setBalance(newBalance);
        return accountRepository.saveAndFlush(account);
    }

    // Task D: Saving a detached stale account copy to demonstrate optimistic lock conflict
    @Transactional
    public Account saveStaleAccount(Account staleAccount) {
        return accountRepository.saveAndFlush(staleAccount);
    }
}
