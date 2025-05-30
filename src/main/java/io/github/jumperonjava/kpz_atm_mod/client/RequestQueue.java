package io.github.jumperonjava.kpz_atm_mod.client;

import com.google.gson.JsonObject;
import io.github.jumperonjava.kpz_atm_mod.packets.ResponsePacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.server.network.EntityTrackerEntry;

import java.util.function.BiConsumer;

public interface RequestQueue  {
    void request(String endpoint, Object data, BiConsumer<ResponsePacket, JsonObject> packet);
}
