package securebankapp.service;

import securebankapp.model.Account;
import securebankapp.model.Transaction;
import securebankapp.storage.FileStorage;
import securebankapp.util.InputValidator;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class BankService {
    private final FileStorage fileStorage;
    private final Map<String, Account> accounts;
    private final List<Transaction> transactions;
    private final SecureRandom secureRandom = new SecureRandom();

    public BankService(FileStorage fileStorage) {
        this.fileStorage = fileStorage;
        this.accounts = new LinkedHashMap<>(fileStorage.loadAccounts());
        this.transactions = new ArrayList<>(fileStorage.loadTransactions());
    }

    public Account createAccount(String username) {
        String accountNumber;
        do {
            accountNumber = generateAccountNumber();
        } while (accounts.containsKey(accountNumber));

        Account account = new Account(accountNumber, username, BigDecimal.ZERO.setScale(2));
        accounts.put(accountNumber, account);
        transactions.add(new Transaction(
                UUID.randomUUID().toString(),
                accountNumber,
                "CREATE",
                BigDecimal.ZERO.setScale(2),
                account.getBalance(),
                LocalDateTime.now(),
                "Account opened"
        ));
        persist();
        return account;
    }

    public List<Account> getAccountsForUser(String username) {
        return accounts.values().stream()
                .filter(account -> account.getUsername().equalsIgnoreCase(username))
                .sorted(Comparator.comparing(Account::getAccountNumber))
                .collect(Collectors.toList());
    }

    public Account deposit(String username, String accountNumber, BigDecimal amount) {
        Account account = requireOwnedAccount(username, accountNumber);
        BigDecimal validAmount = InputValidator.requirePositiveMoney(amount);

        account.deposit(validAmount);
        transactions.add(new Transaction(
                UUID.randomUUID().toString(),
                accountNumber,
                "DEPOSIT",
                validAmount,
                account.getBalance(),
                LocalDateTime.now(),
                "Funds deposited"
        ));
        persist();
        return account;
    }

    public Account withdraw(String username, String accountNumber, BigDecimal amount) {
        Account account = requireOwnedAccount(username, accountNumber);
        BigDecimal validAmount = InputValidator.requirePositiveMoney(amount);

        if (account.getBalance().compareTo(validAmount) < 0) {
            throw new IllegalArgumentException("Insufficient funds.");
        }

        account.withdraw(validAmount);
        transactions.add(new Transaction(
                UUID.randomUUID().toString(),
                accountNumber,
                "WITHDRAW",
                validAmount,
                account.getBalance(),
                LocalDateTime.now(),
                "Funds withdrawn"
        ));
        persist();
        return account;
    }

    public List<Transaction> getTransactions(String username, String accountNumber) {
        requireOwnedAccount(username, accountNumber);
        return transactions.stream()
                .filter(transaction -> transaction.getAccountNumber().equals(accountNumber))
                .sorted(Comparator.comparing(Transaction::getTimestamp))
                .collect(Collectors.toList());
    }

    private Account requireOwnedAccount(String username, String accountNumber) {
        Optional<Account> account = Optional.ofNullable(accounts.get(accountNumber));
        if (!account.isPresent() || !account.get().getUsername().equalsIgnoreCase(username)) {
            throw new IllegalArgumentException("Account not found.");
        }
        return account.get();
    }

    private void persist() {
        fileStorage.saveAccounts(accounts);
        fileStorage.saveTransactions(transactions);
    }

    private String generateAccountNumber() {
        long number = 1_000_000_000L + secureRandom.nextInt(900_000_000);
        return "SB" + number;
    }
}
