package io.github.jumperonjava.kpz_atm_mod.endpoints.bank;

import io.github.jumperonjava.kpz_atm_mod.AtmMod;
import io.github.jumperonjava.kpz_atm_mod.endpoints.EndpointException;
import io.github.jumperonjava.kpz_atm_mod.endpoints.Status;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Map;

public class BankService {
    private static BankService instance;
    private final Database database;
    private final SecureRandom random;

    private BankService() {
        this.database = new Database("root", "", "jdbc:mysql://localhost:3306/bankdatabase");
        this.random = new SecureRandom();
    }

    public static BankService getInstance() {
        if (instance == null) {
            instance = new BankService();
        }
        return instance;
    }

    public Object registerUser(String username, String password) {
        var ref = new Object() {
            String token;
        };

        try {

            database.query("SELECT COUNT(*) as count FROM users WHERE username = ?", username)
                    .accept(resultSet -> {
                        try {
                            if (!resultSet.next()) {
                                throw new EndpointException(Status.ERROR, "user_exists");
                            }
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });

            String hashedPassword = AtmMod.hashPassword(password);
            String token = generateToken();

            database.updateOrFail("INSERT INTO users (username, password, token, balance) VALUES (?, ?, ?, ?)",
                            username, hashedPassword, token, 0.0);

            ref.token = token;
        } catch (Exception e) {
            if (e instanceof EndpointException) {
                throw e;
            }
            throw new EndpointException(Status.ERROR_UNEXPECTED, "db_error");
        }

        return Map.of("token",ref.token);
    }

    public Object login(String username, String password) {
        String hashedPassword = AtmMod.hashPassword(password);

        var ref = new Object() {
            String token = null;
        };

        database.query("SELECT id, username, balance FROM users WHERE username = ? AND password = ?",
                        username, hashedPassword)
                .accept(resultSet -> {
                    try {
                        if (resultSet.next()) {
                            String newToken = generateToken();

                            database.updateOrFail("UPDATE users SET token = ? WHERE username = ?", newToken, username);

                            ref.token = newToken;
                        } else {
                            throw new EndpointException(Status.ERROR, "wrong_credentials");
                        }
                    } catch (SQLException e) {
                        throw new EndpointException(Status.ERROR_UNEXPECTED, "db_error");
                    }
                });

        return Map.of("token",ref.token);
    }

    public Object depositMoney(double amount, String token) {
        if (amount <= 0) {
            throw new EndpointException(Status.ERROR, "invalid_amount");
        }

        try {
            double newBalance = getBalance(token) + amount;
            database.updateOrFail("UPDATE users SET balance = ? WHERE token = ?", newBalance, token);


        } catch (Exception e) {
            if (e instanceof EndpointException) {
                throw e;
            }
            throw new EndpointException(Status.ERROR_UNEXPECTED, "db_error");
        }

        return Map.of();
    }

    public Object withdrawMoney(double amount, String token) {
        if (amount <= 0) {
            throw new EndpointException(Status.ERROR, "invalid_amount");
        }

        try {
            double newBalance = getBalance(token) - amount;
            if (newBalance < 0) {
                throw new EndpointException(Status.ERROR, "not_enough_balance");
            }
            database.updateOrFail("UPDATE users SET balance = ? WHERE token = ?", newBalance, token);

        } catch (Exception e) {
            if (e instanceof EndpointException) {
                throw e;
            }
            throw new EndpointException(Status.ERROR_UNEXPECTED, "Database error during withdrawal");
        }

        return Map.of();
    }

    private double getBalance(String token) {
        var user = new Object() {
            Double currentBalance = null;
        };

        database.query("SELECT balance FROM users WHERE token = ?", token)
                .accept(resultSet -> {
                    try {
                        if (resultSet.next()) {
                            user.currentBalance = resultSet.getDouble("balance");
                        } else {
                            throw new EndpointException(Status.ERROR, "invalid_token");
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });

        return user.currentBalance;
    }

    public Object transferMoney(double amount, String senderToken, String receiverUsername) {
        if (amount <= 0) {
            throw new EndpointException(Status.ERROR, "invalid_amount");
        }

        database.query("SELECT username, token FROM users WHERE token = ? AND username = ?", senderToken, receiverUsername).accept(resultSet -> {
            try {
                if(resultSet.next()){
                    throw new EndpointException(Status.ERROR, "cannot_send_self");
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        try {
            var senderBalance = getBalance(senderToken);

            var receiver = new Object() {
                Double balance = null;
            };

            database.query("SELECT balance FROM users WHERE username = ?", receiverUsername)
                    .accept(resultSet -> {
                        try {
                            if (resultSet.next()) {
                                receiver.balance = resultSet.getDouble("balance");
                            } else {
                                throw new EndpointException(Status.ERROR, "receiver_not_found");
                            }
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });

            if (senderBalance < amount) {
                throw new EndpointException(Status.ERROR, "too_much");
            }

            double newSenderBalance = senderBalance - amount;
            double newReceiverBalance = receiver.balance + amount;

            database.updateOrFail("UPDATE users SET balance = ? WHERE token = ?", newSenderBalance, senderToken);

            database.updateOrFail("UPDATE users SET balance = ? WHERE username = ?", newReceiverBalance, receiverUsername);

        } catch (Exception e) {
            if (e instanceof EndpointException) {
                throw e;
            }
            throw new EndpointException(Status.ERROR_UNEXPECTED, "db_error");
        }

        return Map.of();
    }

    public Object getUserBalance(String token) {
        var rf = new Object() {
            double balance = 0.0;
        };

        database.query("SELECT balance FROM users WHERE token = ?", token)
                .accept(resultSet -> {
                    try {
                        if (resultSet.next()) {
                            rf.balance = resultSet.getDouble("balance");
                        } else {
                            throw new EndpointException(Status.ERROR, "user_error");
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });

        return Map.of("balance", rf.balance);
    }

    private String generateToken() {
        byte[] tokenBytes = new byte[32];
        random.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

}