package io.github.jumperonjava.kpz_atm_mod.client;

import io.github.jumperonjava.kpz_atm_mod.packets.OpenAtmS2CPacket;
import net.fabricmc.api.ClientModInitializer;

public class AtmModClientInit implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        OpenAtmS2CPacket.registerClientReceive();
    }
}
