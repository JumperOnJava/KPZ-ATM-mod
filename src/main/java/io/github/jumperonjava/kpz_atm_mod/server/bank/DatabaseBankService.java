package io.github.jumperonjava.kpz_atm_mod.server.bank;

import io.github.jumperonjava.kpz_atm_mod.server.Status;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class DatabaseBankService implements BankService {
    private final DatabaseUtil databaseUtil;

    public DatabaseBankService(DatabaseUtil db) {
        this.databaseUtil = db;
    }

    private static String hashPassword(String password) {
        try {
            var md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public String registerUser(String username, String password) {
        var ref = new Object() {
            String token;
        };

        try {

            databaseUtil.query("SELECT COUNT(*) as count FROM users WHERE username = ?", username)
                    .accept(resultSet -> {
                        try {
                            if (!resultSet.next()) {
                                throw new EndpointException(Status.ERROR, "user_exists");
                            }
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });

            String hashedPassword = hashPassword(password);
            String token = generateToken();

            databaseUtil.updateOrFail("INSERT INTO users (username, password, token, balance) VALUES (?, ?, ?, ?)",
                    username, hashedPassword, token, 0.0);

            ref.token = token;
        } catch (Exception e) {
            if (e instanceof EndpointException) {
                throw e;
            }
            throw new EndpointException(Status.ERROR_UNEXPECTED, "db_error");
        }

        return ref.token;
    }

    public String login(String username, String password) {
        String hashedPassword = hashPassword(password);

        var ref = new Object() {
            String token = null;
        };

        databaseUtil.query("SELECT id, username, balance FROM users WHERE username = ? AND password = ?",
                        username, hashedPassword)
                .accept(resultSet -> {
                    try {
                        if (resultSet.next()) {
                            String newToken = generateToken();

                            databaseUtil.updateOrFail("UPDATE users SET token = ? WHERE username = ?", newToken, username);

                            ref.token = newToken;
                        } else {
                            throw new EndpointException(Status.ERROR, "wrong_credentials");
                        }
                    } catch (SQLException e) {
                        throw new EndpointException(Status.ERROR_UNEXPECTED, "db_error");
                    }
                });

        return ref.token;
    }


    private long getUserIdByField(String fieldName, String value, String errorCode) {
        if (value == null || value.isEmpty()) {
            throw new EndpointException(Status.ERROR, "invalid_" + fieldName);
        }

        var ref = new Object() {
            long userId = -1;
        };

        String query = "SELECT id FROM users WHERE " + fieldName + " = ?";

        databaseUtil.query(query, value).accept(resultSet -> {
            try {
                if (resultSet.next()) {
                    ref.userId = resultSet.getInt("id");
                }
            } catch (SQLException e) {
                throw new EndpointException(Status.ERROR_UNEXPECTED, "db_error");
            }
        });

        if (ref.userId == -1) {
            throw new EndpointException(Status.ERROR_UNEXPECTED, errorCode);
        }

        return ref.userId;
    }

    private String getUsernameById(long id) {
        if (id == 0) {
            return "";
        }

        var ref = new Object() {
            String username = "";
        };

        String query = "SELECT username FROM users WHERE id = ?";

        databaseUtil.query(query, id).accept(resultSet -> {
            try {
                if (resultSet.next()) {
                    ref.username = resultSet.getString("username");
                }
            } catch (SQLException e) {
                throw new EndpointException(Status.ERROR_UNEXPECTED, "db_error");
            }
        });

        return ref.username;
    }

    public long getUserIdByToken(String token) {
        return getUserIdByField("token", token, "invalid_token");
    }

    public long getUserIdByUsername(String username) {
        return getUserIdByField("username", username, "invalid_username");
    }


    public void setBalance(long id, double newBalance) {
        try {
            if (newBalance < 0) {
                throw new EndpointException(Status.ERROR, "balance_negative");
            }
            databaseUtil.updateOrFail("UPDATE users SET balance = ? WHERE id = ?", newBalance, id);

        } catch (Exception e) {
            if (e instanceof EndpointException) {
                throw e;
            }
            throw new EndpointException(Status.ERROR_UNEXPECTED, "Database error during withdrawal");
        }
    }

    public double getBalance(long id) {
        var user = new Object() {
            Double currentBalance = null;
        };

        databaseUtil.query("SELECT balance FROM users WHERE id = ?", id)
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

    public void logOperation(long from, long to, double amount, String type) {
        databaseUtil.updateSilent("INSERT INTO operations (`from`,`to`,amount,type) VALUES (?,?,?,?)", from, to, amount, type);
    }

    private String generateToken() {
        byte[] tokenBytes = new byte[32];
        new SecureRandom().nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    public Object[] getOperations(long user) {
        List<Map<String,Object>> operations = new ArrayList<>();
        databaseUtil.query("SELECT `from`, `to`, amount, type, date FROM operations WHERE `from` = ? OR `to` = ?", user, user).accept(resultSet -> {
            try {
                while (resultSet.next()) {
                    operations.add(Map.of(
                            "from"  , getUsernameById(resultSet.getLong("from")),
                            "to"    , getUsernameById(resultSet.getLong("to")),
                            "amount", resultSet.getDouble("amount"),
                            "type"  , resultSet.getString("type"),
                            "date"  , resultSet.getString("date")
                    ));
                }
            } catch (SQLException e) {
                throw new EndpointException(Status.ERROR_UNEXPECTED, "db_error");
            }
        });

        return operations.toArray();
    }
}