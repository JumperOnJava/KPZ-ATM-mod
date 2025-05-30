package io.github.jumperonjava.kpz_atm_mod.server.networking;

import com.google.gson.JsonObject;
import io.github.jumperonjava.kpz_atm_mod.server.Status;
import io.github.jumperonjava.kpz_atm_mod.server.bank.BankEndpoints;
import io.github.jumperonjava.kpz_atm_mod.server.bank.EndpointException;

import java.util.HashMap;
import java.util.Map;

public class RequestEndpointAdapter implements RequestHandlerProvider {

    private final BankEndpoints bankEndpoints;

    public RequestEndpointAdapter(BankEndpoints endpoints) {
        this.bankEndpoints = endpoints;
    }

    public Map<String, RequestTypeHandler> getRequestHandlers() {
        var requestTypes = new HashMap<String, RequestTypeHandler>();

        requestTypes.put("login", (ctx,body)->{
            String username = getRequiredString(body, "username");
            String password = getRequiredString(body, "password");
            return Map.of("token", bankEndpoints.login(username, password));
        });

        requestTypes.put("register", (ctx,body)->{
            String username = getRequiredString(body, "username");
            String password = getRequiredString(body, "password");
            return Map.of("token", bankEndpoints.register(username, password));
        });

        requestTypes.put("deposit",  (ctx,body)->{
            double amount = Math.floor(getRequiredDouble(body, "amount"));
            String token = getRequiredString(body, "token");
            bankEndpoints.deposit((int) amount,token,ctx.player());
            return Map.of();
        });

        requestTypes.put("withdraw",  (ctx,body)->{
            double amount = Math.floor(getRequiredDouble(body, "amount"));
            String token = getRequiredString(body, "token");
            bankEndpoints.withdraw((int) amount,token,ctx.player());
            return Map.of();
        });

        requestTypes.put("transfer", (ctx, body)->{
            double amount = getRequiredDouble(body, "amount");
            String token = getRequiredString(body, "token");
            String receiverUsername = getRequiredString(body, "receiver");
            bankEndpoints.transfer((int) amount,token,receiverUsername);
            return Map.of();
        });

        requestTypes.put("balance",  (ctx,body)->{
            String token = getRequiredString(body, "token");
            return Map.of("balance", bankEndpoints.balance(token));
        });

        requestTypes.put("history" ,(ctx, body)->{
            String token = getRequiredString(body, "token");
            return  Map.of("history", bankEndpoints.getHistory(token));
        });

        return requestTypes;
    }

    private static String getRequiredString(JsonObject json, String key) {
        if (!json.has(key) || json.get(key).isJsonNull()) {
            throw new EndpointException(Status.ERROR, "Missing required parameter: " + key);
        }
        return json.get(key).getAsString();
    }

    private static double getRequiredDouble(JsonObject json, String key) {
        if (!json.has(key) || json.get(key).isJsonNull()) {
            throw new EndpointException(Status.ERROR, "Missing required parameter: " + key);
        }
        return json.get(key).getAsDouble();
    }
}
