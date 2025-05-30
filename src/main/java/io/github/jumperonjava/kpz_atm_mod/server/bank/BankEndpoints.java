package io.github.jumperonjava.kpz_atm_mod.server.bank;

import net.minecraft.server.network.ServerPlayerEntity;

public interface BankEndpoints {
    String login(String username, String password);

    String register(String username, String password);

    void deposit(int amount, String token, ServerPlayerEntity player) throws InterruptedException;

    void withdraw(int amount, String token, ServerPlayerEntity player) throws InterruptedException;

    void transfer(int amount, String token, String receiverUsername);

    Object getHistory(String token);

    int balance(String token);
}
