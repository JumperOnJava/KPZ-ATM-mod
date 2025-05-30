package io.github.jumperonjava.kpz_atm_mod.server.bank;

import io.github.jumperonjava.kpz_atm_mod.AtmMod;
import io.github.jumperonjava.kpz_atm_mod.server.networking.ServerThreadExecutor;
import io.github.jumperonjava.kpz_atm_mod.server.Status;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

public class DefaultBankEndpoints implements BankEndpoints {

    private final DatabaseBankService bankService;
    private final ServerThreadExecutor serverThreadExecutor;

    public DefaultBankEndpoints(DatabaseBankService bankService, ServerThreadExecutor serverThreadExecutor) {
        this.bankService = bankService;
        this.serverThreadExecutor = serverThreadExecutor;
    }

    @Override
    public String login(String username, String password) {
        return bankService.login(username, password);
    }

    @Override
    public String register(String username, String password) {
        return bankService.registerUser(username, password);
    }

    @Override
    public void deposit(int amount, String token, ServerPlayerEntity player) throws InterruptedException {

        serverThreadExecutor.runOnServerThread(() -> {
            var diamondCount = player.getInventory().count(Items.DIAMOND);
            if (diamondCount < amount) {
                throw new EndpointException(Status.ERROR,"not_enough_diamonds");
            }
            player.getInventory().remove(stack->stack.getItem()==Items.DIAMOND, (int) amount, player.playerScreenHandler.getCraftingInput());
        }).await();

        var user = bankService.getUserIdByToken(token);
        var balance = bankService.getBalance(user);

        bankService.setBalance(user,balance + amount);
        bankService.logOperation(0,user, amount, "deposit");
    }

    @Override
    public void withdraw(int amount, String token, ServerPlayerEntity player) throws InterruptedException {


        var user = bankService.getUserIdByToken(token);
        var balance = bankService.getBalance(user);

        if(amount > balance) {
            throw new EndpointException(Status.ERROR, "not_enough_balance");
        }

        serverThreadExecutor.runOnServerThread(() -> {
            var freeSlots = new Object() {
                int value = 0;
            };
            player.getInventory().getMainStacks().forEach((stack)->{
                if(stack.isEmpty()){
                    freeSlots.value +=64;
                } else if (stack.getItem()==Items.DIAMOND) {
                    freeSlots.value += 64 - stack.getCount();
                }
            });
            AtmMod.LOGGER.info("Free slot amount: {} out of {} needed", freeSlots.value, amount);

            if(freeSlots.value >= amount){
                player.getInventory().insertStack(new ItemStack(Items.DIAMOND, amount));
            }
            else {
                throw new EndpointException(Status.ERROR, "not_enough_inventory_space");
            }

        }).await();

        bankService.setBalance(user,balance - amount);
        bankService.logOperation(user,0, amount, "withdraw");
    }

    @Override
    public void transfer(int amount, String token, String receiverUsername) {
        var sender = bankService.getUserIdByToken(token);
        var receiver = bankService.getUserIdByUsername(receiverUsername);

        var senderBalance = bankService.getBalance(sender);
        var receiverBalance = bankService.getBalance(receiver);

        if (senderBalance < amount) {
            throw new EndpointException(Status.ERROR, "not_enough_balance");
        }

        senderBalance -= amount;
        receiverBalance += amount;

        bankService.setBalance(sender, senderBalance);
        bankService.setBalance(receiver, receiverBalance);
        bankService.logOperation(sender, receiver, amount, "transfer");
    }

    @Override
    public Object getHistory(String token) {
        long user = bankService.getUserIdByToken(token);
        return bankService.getOperations(user);
    }

    @Override
    public int balance(String token) {
        return (int) bankService.getBalance(bankService.getUserIdByToken(token));
    }


}
