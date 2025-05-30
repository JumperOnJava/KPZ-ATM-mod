package io.github.jumperonjava.kpz_atm_mod.server.bank;

import io.github.jumperonjava.kpz_atm_mod.server.endpoints.EndpointException;
import io.github.jumperonjava.kpz_atm_mod.server.Status;

import java.sql.*;
import java.util.function.Consumer;

public class DatabaseUtil {
    String username;
    String password;
    String host;

    public DatabaseUtil(String username, String password, String host) {
        this.username = username;
        this.password = password;
        this.host = host;
    }

    public Consumer<Consumer<ResultSet>> query(String sql, Object... args) {
        return (callback) -> {
            try (Connection conn = DriverManager.getConnection(host, username, password)) {
                PreparedStatement ps = conn.prepareStatement(sql);
                for (int i = 0; i < args.length; i++) {
                    ps.setObject(i + 1, args[i]);
                }
                callback.accept(ps.executeQuery());
            } catch (SQLException e) {
                throw new EndpointException(Status.ERROR_UNEXPECTED, e.getMessage());
            }
        };
    }

    public void updateOrFail(String sql, Object... args) {
        update(sql, true, args);
    }

    public void updateSilent(String sql, Object... args) {
        update(sql, false, args);
    }

    public void update(String sql, boolean throwIfNoChanges, Object... args) {
        try (Connection conn = DriverManager.getConnection(host, username, password)) {
            PreparedStatement ps = conn.prepareStatement(sql);
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            var update = ps.executeUpdate();
            if(throwIfNoChanges){
                if(update == 0){
                  throw new EndpointException(Status.ERROR_UNEXPECTED, "db_update_fail");
                }
            }
        } catch (SQLException e) {
            throw new EndpointException(Status.ERROR_UNEXPECTED, e.getMessage());
        }
    }
}
