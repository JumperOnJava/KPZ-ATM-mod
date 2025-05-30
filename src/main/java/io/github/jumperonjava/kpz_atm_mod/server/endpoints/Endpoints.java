package io.github.jumperonjava.kpz_atm_mod.server.endpoints;

import com.google.gson.JsonObject;
import io.github.jumperonjava.kpz_atm_mod.AtmMod;
import io.github.jumperonjava.kpz_atm_mod.server.ServerThreadExecutor;
import io.github.jumperonjava.kpz_atm_mod.server.Status;
import io.github.jumperonjava.kpz_atm_mod.server.bank.DatabaseBankService;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.HashMap;
import java.util.Map;

public class Endpoints implements EndpointProvider {

    private final DatabaseBankService bankService;
    private final ServerThreadExecutor serverThreadExecutor;

    public Endpoints(DatabaseBankService bankService, ServerThreadExecutor serverThreadExecutor) {
        this.bankService = bankService;
        this.serverThreadExecutor = serverThreadExecutor;
    }


    private Object login(ServerPlayNetworking.Context context, JsonObject body) {
        String username = getRequiredString(body, "username");
        String password = getRequiredString(body, "password");
        var token = bankService.login(username, password);
        return Map.of("token", token);
    }

    private Object register(ServerPlayNetworking.Context context, JsonObject body) {
        String username = getRequiredString(body, "username");
        String password = getRequiredString(body, "password");

        var token = bankService.registerUser(username, password);
        return Map.of("token", token);
    }

    private Object deposit(ServerPlayNetworking.Context context, JsonObject body) throws InterruptedException {
        double amount = Math.floor(getRequiredDouble(body, "amount"));
        String token = getRequiredString(body, "token");

        serverThreadExecutor.runOnServerThread(() -> {
            var diamondCount = context.player().getInventory().count(Items.DIAMOND);
            if (diamondCount < amount) {
                throw new EndpointException(Status.ERROR,"not_enough_diamonds");
            }
            context.player().getInventory().remove(stack->stack.getItem()==Items.DIAMOND, (int) amount, context.player().playerScreenHandler.getCraftingInput());
        }).await();

        var user = bankService.getUserIdByToken(token);
        var balance = bankService.getBalance(user);

        bankService.setBalance(user,balance + amount);
        bankService.logOperation(0,user, amount, "deposit");

        return Map.of();
    }

    private Object withdraw(ServerPlayNetworking.Context context, JsonObject body) throws InterruptedException {
        int amount = (int) getRequiredDouble(body, "amount");
        String token = getRequiredString(body, "token");

        var user = bankService.getUserIdByToken(token);
        var balance = bankService.getBalance(user);

        if(amount > balance) {
            throw new EndpointException(Status.ERROR, "not_enough_balance");
        }

        serverThreadExecutor.runOnServerThread(() -> {
            var freeSlots = new Object() {
                int value = 0;
            };
            context.player().getInventory().getMainStacks().forEach((stack)->{
                if(stack.isEmpty()){
                    freeSlots.value +=64;
                } else if (stack.getItem()==Items.DIAMOND) {
                    freeSlots.value += 64 - stack.getCount();
                }
            });
            AtmMod.LOGGER.info("Free slot amount: {} out of {} needed", freeSlots.value, amount);

            if(freeSlots.value >= amount){
                context.player().getInventory().insertStack(new ItemStack(Items.DIAMOND, amount));
            }
            else {
                throw new EndpointException(Status.ERROR, "not_enough_inventory_space");
            }

        }).await();

        bankService.setBalance(user,balance - amount);
        bankService.logOperation(user,0, amount, "withdraw");
        return Map.of();
    }

    private Object transfer(ServerPlayNetworking.Context context, JsonObject body) {
        double amount = getRequiredDouble(body, "amount");
        String token = getRequiredString(body, "token");
        String receiverUsername = getRequiredString(body, "receiver");

        var sender = bankService.getUserIdByToken(token);
        var receiver = bankService.getUserIdByUsername(receiverUsername);

        var senderBalance = bankService.getBalance(receiver);
        var receiverBalance = bankService.getBalance(sender);

        if (senderBalance < amount) {
            throw new EndpointException(Status.ERROR, "not_enough_balance");
        }

        senderBalance -= amount;
        receiverBalance += amount;

        bankService.setBalance(sender, senderBalance);
        bankService.setBalance(receiver, receiverBalance);
        bankService.logOperation(sender, receiver, amount, "transfer");

        return Map.of();
    }

    private Object getHistory(ServerPlayNetworking.Context context, JsonObject body) {
        String token = getRequiredString(body, "token");
        long user = bankService.getUserIdByToken(token);
        return Map.of("history", bankService.getOperations(user));
    }

    private Object balance(ServerPlayNetworking.Context context, JsonObject body) {
        String token = getRequiredString(body, "token");
        return Map.of("balance", bankService.getBalance(bankService.getUserIdByToken(token)));
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

    @Override
    public Map<String, Endpoint> getEndpoints() {
        var endpoints = new HashMap<String, Endpoint>();

        endpoints.put("login", this::login);
        endpoints.put("register", this::register);
        endpoints.put("deposit", this::deposit);
        endpoints.put("withdraw", this::withdraw);
        endpoints.put("transfer", this::transfer);
        endpoints.put("balance", this::balance);
        endpoints.put("history", this::getHistory);

        return endpoints;
    }
}
