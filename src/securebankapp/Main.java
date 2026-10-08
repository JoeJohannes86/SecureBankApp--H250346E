package securebankapp;

import securebankapp.model.Account;
import securebankapp.model.Transaction;
import securebankapp.model.User;
import securebankapp.service.AuthService;
import securebankapp.service.BankService;
import securebankapp.storage.FileStorage;
import securebankapp.util.InputValidator;

import java.io.Console;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {
    private final Scanner scanner = new Scanner(System.in);
    private final AuthService authService;
    private final BankService bankService;

    public Main(AuthService authService, BankService bankService) {
        this.authService = authService;
        this.bankService = bankService;
    }

    public static void main(String[] args) {
        Path dataDirectory = Paths.get("data");
        FileStorage fileStorage = new FileStorage(dataDirectory);
        AuthService authService = new AuthService(fileStorage);
        BankService bankService = new BankService(fileStorage);

        new Main(authService, bankService).run();
    }

    private void run() {
        System.out.println("=================================");
        System.out.println(" Secure Banking Application");
        System.out.println("=================================");

        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("1. Register user");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            int choice = readMenuChoice("Choose an option: ", 1, 3);

            switch (choice) {
                case 1:
                    registerUser();
                    break;
                case 2:
                    login();
                    break;
                case 3:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }

        System.out.println("Thank you for using Secure Banking Application.");
    }

    private void registerUser() {
        System.out.print("Choose a username: ");
        String username = scanner.nextLine().trim();

        char[] password = readPassword("Choose a password: ");

        try {
            User user = authService.register(username, password);
            System.out.println("User created successfully: " + user.getUsername());
        } catch (IllegalArgumentException | IllegalStateException ex) {
            System.out.println("Registration failed: " + ex.getMessage());
        }
    }

    private void login() {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        char[] password = readPassword("Password: ");

        Optional<User> user = authService.login(username, password);
        if (!user.isPresent()) {
            System.out.println("Invalid username or password.");
            return;
        }

        System.out.println("Login successful. Welcome, " + user.get().getUsername() + ".");
        showBankingMenu(user.get());
    }

    private void showBankingMenu(User user) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println();
            System.out.println("1. Create bank account");
            System.out.println("2. View account balance");
            System.out.println("3. Deposit funds");
            System.out.println("4. Withdraw funds");
            System.out.println("5. View transaction history");
            System.out.println("6. Logout");
            int choice = readMenuChoice("Choose an option: ", 1, 6);

            switch (choice) {
                case 1:
                    createAccount(user);
                    break;
                case 2:
                    viewBalance(user);
                    break;
                case 3:
                    deposit(user);
                    break;
                case 4:
                    withdraw(user);
                    break;
                case 5:
                    viewTransactions(user);
                    break;
                case 6:
                    loggedIn = false;
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private void createAccount(User user) {
        Account account = bankService.createAccount(user.getUsername());
        System.out.println("Account created. Account number: " + account.getAccountNumber());
    }

    private void viewBalance(User user) {
        Optional<Account> account = chooseAccount(user);
        account.ifPresent(value -> System.out.println("Balance: " + value.getBalance().toPlainString()));
    }

    private void deposit(User user) {
        Optional<Account> account = chooseAccount(user);
        if (!account.isPresent()) {
            return;
        }

        BigDecimal amount = readMoneyAmount("Deposit amount: ");
        try {
            Account updated = bankService.deposit(user.getUsername(), account.get().getAccountNumber(), amount);
            System.out.println("Deposit successful. New balance: " + updated.getBalance().toPlainString());
        } catch (IllegalArgumentException ex) {
            System.out.println("Deposit failed: " + ex.getMessage());
        }
    }

    private void withdraw(User user) {
        Optional<Account> account = chooseAccount(user);
        if (!account.isPresent()) {
            return;
        }

        BigDecimal amount = readMoneyAmount("Withdrawal amount: ");
        try {
            Account updated = bankService.withdraw(user.getUsername(), account.get().getAccountNumber(), amount);
            System.out.println("Withdrawal successful. New balance: " + updated.getBalance().toPlainString());
        } catch (IllegalArgumentException ex) {
            System.out.println("Withdrawal failed: " + ex.getMessage());
        }
    }

    private void viewTransactions(User user) {
        Optional<Account> account = chooseAccount(user);
        if (!account.isPresent()) {
            return;
        }

        List<Transaction> transactions = bankService.getTransactions(user.getUsername(), account.get().getAccountNumber());
        if (transactions.isEmpty()) {
            System.out.println("No transactions found for this account.");
            return;
        }

        System.out.println("Transaction History");
        for (Transaction transaction : transactions) {
            System.out.printf("%s | %s | %s | Balance after: %s | %s%n",
                    transaction.getTimestamp(),
                    transaction.getType(),
                    transaction.getAmount().toPlainString(),
                    transaction.getBalanceAfter().toPlainString(),
                    transaction.getNote());
        }
    }

    private Optional<Account> chooseAccount(User user) {
        List<Account> accounts = bankService.getAccountsForUser(user.getUsername());
        if (accounts.isEmpty()) {
            System.out.println("No accounts found. Please create an account first.");
            return Optional.empty();
        }

        System.out.println("Your accounts:");
        for (int i = 0; i < accounts.size(); i++) {
            Account account = accounts.get(i);
            System.out.printf("%d. %s - Balance: %s%n",
                    i + 1,
                    account.getAccountNumber(),
                    account.getBalance().toPlainString());
        }

        int selected = readMenuChoice("Select account: ", 1, accounts.size());
        return Optional.of(accounts.get(selected - 1));
    }

    private int readMenuChoice(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // Keep asking until the user enters a valid menu number.
            }
            System.out.printf("Enter a number from %d to %d.%n", min, max);
        }
    }

    private BigDecimal readMoneyAmount(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return InputValidator.requireValidMoney(input);
            } catch (IllegalArgumentException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    private char[] readPassword(String prompt) {
        Console console = System.console();
        if (console != null) {
            String hiddenPrompt = prompt.replace(": ", " (input hidden): ");
            char[] password = console.readPassword(hiddenPrompt);
            return password == null ? new char[0] : password;
        }

        System.out.print(prompt);
        return scanner.nextLine().toCharArray();
    }
}
