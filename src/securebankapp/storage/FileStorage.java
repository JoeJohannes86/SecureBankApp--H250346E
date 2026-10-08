package securebankapp.storage;

import securebankapp.model.Account;
import securebankapp.model.Transaction;
import securebankapp.model.User;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FileStorage {
    private static final String DELIMITER = "\\|";

    private final Path usersFile;
    private final Path accountsFile;
    private final Path transactionsFile;

    public FileStorage(Path dataDirectory) {
        try {
            Files.createDirectories(dataDirectory);
            this.usersFile = dataDirectory.resolve("users.txt");
            this.accountsFile = dataDirectory.resolve("accounts.txt");
            this.transactionsFile = dataDirectory.resolve("transactions.txt");
            createFileIfMissing(usersFile);
            createFileIfMissing(accountsFile);
            createFileIfMissing(transactionsFile);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not initialize data files.", ex);
        }
    }

    public Map<String, User> loadUsers() {
        Map<String, User> users = new LinkedHashMap<>();
        for (String line : readAllLines(usersFile)) {
            if (shouldSkip(line)) {
                continue;
            }

            String[] parts = line.split(DELIMITER, -1);
            if (parts.length == 3) {
                User user = new User(parts[0], parts[1], parts[2]);
                users.put(user.getUsername().toLowerCase(), user);
            }
        }
        return users;
    }

    public void saveUsers(Map<String, User> users) {
        List<String> lines = new ArrayList<>();
        for (User user : users.values()) {
            lines.add(String.join("|", user.getUsername(), user.getSalt(), user.getPasswordHash()));
        }
        writeAllLines(usersFile, lines);
    }

    public Map<String, Account> loadAccounts() {
        Map<String, Account> accounts = new LinkedHashMap<>();
        for (String line : readAllLines(accountsFile)) {
            if (shouldSkip(line)) {
                continue;
            }

            String[] parts = line.split(DELIMITER, -1);
            if (parts.length == 3) {
                Account account = new Account(parts[0], parts[1], new BigDecimal(parts[2]).setScale(2));
                accounts.put(account.getAccountNumber(), account);
            }
        }
        return accounts;
    }

    public void saveAccounts(Map<String, Account> accounts) {
        List<String> lines = new ArrayList<>();
        for (Account account : accounts.values()) {
            lines.add(String.join("|",
                    account.getAccountNumber(),
                    account.getUsername(),
                    account.getBalance().setScale(2).toPlainString()));
        }
        writeAllLines(accountsFile, lines);
    }

    public List<Transaction> loadTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        for (String line : readAllLines(transactionsFile)) {
            if (shouldSkip(line)) {
                continue;
            }

            String[] parts = line.split(DELIMITER, -1);
            if (parts.length == 7) {
                transactions.add(new Transaction(
                        parts[0],
                        parts[1],
                        parts[2],
                        new BigDecimal(parts[3]).setScale(2),
                        new BigDecimal(parts[4]).setScale(2),
                        LocalDateTime.parse(parts[5]),
                        parts[6]
                ));
            }
        }
        return transactions;
    }

    public void saveTransactions(List<Transaction> transactions) {
        List<String> lines = new ArrayList<>();
        for (Transaction transaction : transactions) {
            lines.add(String.join("|",
                    transaction.getId(),
                    transaction.getAccountNumber(),
                    transaction.getType(),
                    transaction.getAmount().setScale(2).toPlainString(),
                    transaction.getBalanceAfter().setScale(2).toPlainString(),
                    transaction.getTimestamp().toString(),
                    sanitize(transaction.getNote())));
        }
        writeAllLines(transactionsFile, lines);
    }

    private List<String> readAllLines(Path path) {
        try {
            return Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not read file: " + path, ex);
        }
    }

    private void writeAllLines(Path path, List<String> lines) {
        try {
            Files.write(path, lines, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not write file: " + path, ex);
        }
    }

    private void createFileIfMissing(Path path) throws IOException {
        if (Files.notExists(path)) {
            Files.createFile(path);
        }
    }

    private boolean shouldSkip(String line) {
        return line == null || line.isBlank() || line.trim().startsWith("#");
    }

    private String sanitize(String value) {
        return value == null ? "" : value.replace("|", "/").replace("\r", " ").replace("\n", " ");
    }
}
