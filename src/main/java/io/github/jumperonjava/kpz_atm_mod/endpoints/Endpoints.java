package io.github.jumperonjava.kpz_atm_mod.endpoints;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.jumperonjava.kpz_atm_mod.AtmMod;
import io.github.jumperonjava.kpz_atm_mod.endpoints.bank.BankService;
import io.github.jumperonjava.kpz_atm_mod.packets.RequestPacket;
import io.github.jumperonjava.kpz_atm_mod.packets.ResponsePacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import java.util.HashMap;

public class Endpoints implements ServerPlayNetworking.PlayPayloadHandler<RequestPacket> {

    private static Endpoints instance;
    private final HashMap<String, Endpoint> endpoints = new HashMap<>();
    private final BankService bankService;

    private Endpoints() {
        // Initialize all endpoints
        endpoints.put("login", this::login);
        endpoints.put("register", this::register);
        endpoints.put("deposit", this::deposit);
        endpoints.put("withdraw", this::withdraw);
        endpoints.put("transfer", this::transfer);
        endpoints.put("balance", this::balance);

        this.bankService = BankService.getInstance();
    }

    public static Endpoints getInstance() {
        if (instance == null) {
            instance = new Endpoints();
        }
        return instance;
    }

    private Object login(ServerPlayNetworking.Context context, JsonObject body) {
        String username = getRequiredString(body, "username");
        String password = getRequiredString(body, "password");

        return bankService.login(username, password);
    }

    private Object register(ServerPlayNetworking.Context context, JsonObject body) {
        String username = getRequiredString(body, "username");
        String password = getRequiredString(body, "password");

        return bankService.registerUser(username, password);
    }


    private Object deposit(ServerPlayNetworking.Context context, JsonObject body) {
        double amount = getRequiredDouble(body, "amount");
        String token = getRequiredString(body, "token");

        return bankService.depositMoney(amount, token);
    }

    private Object withdraw(ServerPlayNetworking.Context context, JsonObject body) {
        double amount = getRequiredDouble(body, "amount");
        String token = getRequiredString(body, "token");

        int get

        return bankService.withdrawMoney(amount, token);
    }

    private Object transfer(ServerPlayNetworking.Context context, JsonObject body) {
        double amount = getRequiredDouble(body, "amount");
        String token = getRequiredString(body, "token");
        String receiverUserId = getRequiredString(body, "receiver");

        return bankService.transferMoney(amount, token, receiverUserId);
    }

    private Object balance(ServerPlayNetworking.Context context, JsonObject body) {
        String token = getRequiredString(body, "token");

        return bankService.getUserBalance(token);
    }

    @Override
    public void receive(RequestPacket requestPacket, ServerPlayNetworking.Context context) {
        new Thread(() -> {
            ResponsePacket responsePacket = null;
            try {

                Endpoint endpoint = endpoints.get(requestPacket.endpoint());
                if (endpoint == null) {
                    throw new EndpointException(Status.ERROR, "Unknown endpoint: " + requestPacket.endpoint());
                }

                JsonObject requestBody = AtmMod.GSON.fromJson(requestPacket.data(), JsonObject.class);
                Object response = endpoint.handle(context, requestBody);

                responsePacket = new ResponsePacket(
                        requestPacket.id(),
                        Status.SUCCESS,
                        AtmMod.GSON.toJson(response)
                );

            } catch (EndpointException e) {
                JsonObject errorResponse = new JsonObject();
                errorResponse.add("error", new JsonPrimitive(e.message));
                responsePacket = new ResponsePacket(
                        requestPacket.id(),
                        e.status,
                        AtmMod.GSON.toJson(errorResponse)
                );
            } catch (Exception e) {
                JsonObject errorResponse = new JsonObject();
                errorResponse.add("error", new JsonPrimitive(e.getMessage()));
                responsePacket = new ResponsePacket(
                        requestPacket.id(),
                        Status.ERROR_UNEXPECTED,
                        AtmMod.GSON.toJson(errorResponse)
                );
            }

            if (responsePacket != null) {
                ServerPlayNetworking.send(context.player(), responsePacket);
            }
        }).start();
    }

    private String getRequiredString(JsonObject json, String key) {
        if (!json.has(key) || json.get(key).isJsonNull()) {
            throw new EndpointException(Status.ERROR, "Missing required parameter: " + key);
        }
        return json.get(key).getAsString();
    }

    private double getRequiredDouble(JsonObject json, String key) {
        if (!json.has(key) || json.get(key).isJsonNull()) {
            throw new EndpointException(Status.ERROR, "Missing required parameter: " + key);
        }
        return json.get(key).getAsDouble();
    }

}