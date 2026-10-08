package securebankapp.model;

import java.math.BigDecimal;

public class Account {
    private final String accountNumber;
    private final String username;
    private BigDecimal balance;

    public Account(String accountNumber, String username, BigDecimal balance) {
        this.accountNumber = accountNumber;
        this.username = username;
        this.balance = balance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getUsername() {
        return username;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void deposit(BigDecimal amount) {
        balance = balance.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        balance = balance.subtract(amount);
    }
}
