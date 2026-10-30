package kz.iitu.springlab.account;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_name", nullable = false, length = 120)
    private String ownerName;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Version
    private Long version;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    public Account() {
    }

    public Account(String ownerName, BigDecimal balance) {
        this.ownerName = ownerName;
        this.balance = balance;
        this.status = "ACTIVE";
    }

    public Account(Long id, String ownerName, BigDecimal balance, Long version, String status) {
        this.id = id;
        this.ownerName = ownerName;
        this.balance = balance;
        this.version = version;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void debit(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }
        balance = balance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        balance = balance.add(amount);
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", ownerName='" + ownerName + '\'' +
                ", balance=" + balance +
                ", version=" + version +
                ", status='" + status + '\'' +
                '}';
    }
}
