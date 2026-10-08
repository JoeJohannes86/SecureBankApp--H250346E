package securebankapp.service;

import securebankapp.model.User;
import securebankapp.storage.FileStorage;
import securebankapp.util.InputValidator;
import securebankapp.util.SecurityUtil;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class AuthService {
    private final FileStorage fileStorage;
    private final Map<String, User> users;

    public AuthService(FileStorage fileStorage) {
        this.fileStorage = fileStorage;
        this.users = new LinkedHashMap<>(fileStorage.loadUsers());
    }

    public User register(String username, char[] password) {
        try {
            String validUsername = InputValidator.requireValidUsername(username);
            InputValidator.requireStrongPassword(password);

            String key = validUsername.toLowerCase();
            if (users.containsKey(key)) {
                throw new IllegalStateException("Username already exists.");
            }

            byte[] salt = SecurityUtil.generateSalt();
            byte[] hash = SecurityUtil.hashPassword(password, salt);
            User user = new User(
                    validUsername,
                    SecurityUtil.toBase64(salt),
                    SecurityUtil.toBase64(hash)
            );
            users.put(key, user);
            fileStorage.saveUsers(users);
            return user;
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    public Optional<User> login(String username, char[] password) {
        try {
            if (username == null || username.isBlank()) {
                return Optional.empty();
            }

            User user = users.get(username.trim().toLowerCase());
            if (user == null) {
                return Optional.empty();
            }

            byte[] salt = SecurityUtil.fromBase64(user.getSalt());
            byte[] expectedHash = SecurityUtil.fromBase64(user.getPasswordHash());
            byte[] actualHash = SecurityUtil.hashPassword(password, salt);

            if (SecurityUtil.constantTimeEquals(expectedHash, actualHash)) {
                return Optional.of(user);
            }
            return Optional.empty();
        } finally {
            Arrays.fill(password, '\0');
        }
    }
}
