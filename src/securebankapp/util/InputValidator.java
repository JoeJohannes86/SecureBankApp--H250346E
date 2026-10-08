package securebankapp.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Pattern;

public final class InputValidator {
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]{3,20}$");
    private static final Pattern MONEY_PATTERN = Pattern.compile("^\\d{1,9}(\\.\\d{1,2})?$");

    private InputValidator() {
    }

    public static String requireValidUsername(String username) {
        if (username == null) {
            throw new IllegalArgumentException("Username is required.");
        }

        String trimmed = username.trim();
        if (!USERNAME_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Username must be 3-20 characters and use only letters, numbers, or underscores.");
        }
        return trimmed;
    }

    public static void requireStrongPassword(char[] password) {
        if (password == null || password.length < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long.");
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSymbol = false;

        for (char character : password) {
            hasUpper |= Character.isUpperCase(character);
            hasLower |= Character.isLowerCase(character);
            hasDigit |= Character.isDigit(character);
            hasSymbol |= !Character.isLetterOrDigit(character);
        }

        if (!(hasUpper && hasLower && hasDigit && hasSymbol)) {
            throw new IllegalArgumentException("Password must include uppercase, lowercase, number, and symbol characters.");
        }
    }

    public static BigDecimal requireValidMoney(String value) {
        if (value == null || !MONEY_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Enter a valid amount with up to 2 decimal places.");
        }
        return requirePositiveMoney(new BigDecimal(value));
    }

    public static BigDecimal requirePositiveMoney(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
        return amount.setScale(2, RoundingMode.UNNECESSARY);
    }
}
