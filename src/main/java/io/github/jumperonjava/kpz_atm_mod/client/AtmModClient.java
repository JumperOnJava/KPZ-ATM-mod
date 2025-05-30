package io.github.jumperonjava.kpz_atm_mod.client;

import io.github.jumperonjava.kpz_atm_mod.packets.OpenAtmS2CPacket;
import io.github.jumperonjava.kpz_atm_mod.packets.ResponsePacket;
import net.fabricmc.api.ClientModInitializer;

public class AtmModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        var queue = SimpleRequestQueue.getInstance();

        OpenAtmS2CPacket.registerClientReceive(queue);
        ResponsePacket.registerClientReceive(queue);
    }
}
