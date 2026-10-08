# Secure Banking Application

## Description

Secure Banking Application is a console-based Java project that demonstrates object-oriented programming and secure coding practices. It supports user authentication, account management, deposits, withdrawals, and file-based persistence without using a database.

## Student Details

- Name: Katiyo Kudakwashe
- Registration Number: H250346E

## Features

- User registration and login
- Password hashing with PBKDF2, unique salt values, and constant-time password comparison
- Bank account creation
- Account balance viewing
- Deposit and withdrawal transactions
- Transaction history for each account
- File persistence for users, accounts, and transactions
- Input validation for usernames, passwords, menu choices, and money amounts
- Money handling with `BigDecimal` to avoid floating-point errors

## Project Structure

```text
Secure coding banking system/
+-- data/
|   +-- accounts.txt
|   +-- transactions.txt
|   +-- users.txt
+-- src/
|   +-- securebankapp/
|       +-- Main.java
|       +-- model/
|       +-- service/
|       +-- storage/
|       +-- util/
+-- README.md
```

## How to Compile and Run

From the project root, run:

```bash
javac -d out src/securebankapp/*.java src/securebankapp/model/*.java src/securebankapp/service/*.java src/securebankapp/storage/*.java src/securebankapp/util/*.java
java -cp out securebankapp.Main
```

Recommended Java version: Java 8 or newer.

## Data Files

The application stores data in plain text files inside the `data` folder:

- `data/users.txt`: usernames, salts, and password hashes
- `data/accounts.txt`: account numbers, owners, and balances
- `data/transactions.txt`: transaction records

Passwords are never stored in plaintext.

## Security Practices Implemented

- Passwords are hashed using `PBKDF2WithHmacSHA256`
- Each user receives a random salt generated with `SecureRandom`
- Password comparisons use constant-time comparison
- User input is validated before processing
- Account ownership is checked before balance, deposit, withdrawal, or history actions
- Financial values use `BigDecimal`
- File operations use standard Java APIs and UTF-8 encoding

## GitHub Repository

Repository name:

```text
SecureBankApp-H250346E
```
