package io.github.jumperonjava.kpz_atm_mod.client;

import com.google.gson.JsonObject;
import io.github.jumperonjava.kpz_atm_mod.AtmMod;
import io.github.jumperonjava.kpz_atm_mod.packets.RequestPacket;
import io.github.jumperonjava.kpz_atm_mod.packets.ResponsePacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import java.util.HashMap;
import java.util.function.BiConsumer;

public class SimpleRequestQueue implements RequestQueue{
    private static SimpleRequestQueue instance;
    public static SimpleRequestQueue getInstance() {
        if(instance == null){
            instance = new SimpleRequestQueue();
        }
        return instance;
    }

    HashMap<Integer, BiConsumer<ResponsePacket,JsonObject>> queue;
    private SimpleRequestQueue() {
        queue = new HashMap<>();
    }


    int packetNumber = 0;
    public void request(String endpoint, Object data, BiConsumer<ResponsePacket,JsonObject> packet) {
        ClientPlayNetworking.send(new RequestPacket(packetNumber,endpoint, AtmMod.GSON.toJson(data)));
        queue.put(packetNumber++, packet);
    }

    @Override
    public void receive(ResponsePacket packet, ClientPlayNetworking.Context context) {
        queue.getOrDefault(packet.id(),(p,jsonObject)->{
            AtmMod.LOGGER.warn("Received wrong response id {} ",packet.id());
        }).accept(packet, AtmMod.GSON.fromJson(packet.data(), JsonObject.class));
        queue.remove(packet.id());
    }
}
