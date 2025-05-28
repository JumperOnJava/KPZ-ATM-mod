package io.github.jumperonjava.kpz_atm_mod.client;

import com.google.gson.JsonObject;
import io.github.jumperonjava.kpz_atm_mod.AtmModInit;
import io.github.jumperonjava.kpz_atm_mod.packets.RequestPacket;
import io.github.jumperonjava.kpz_atm_mod.packets.ResponsePacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class RequestQueue {
    private static RequestQueue instance;
    public static RequestQueue getInstance() {
        if(instance == null){
            instance = new RequestQueue();
        }
        return instance;
    }

    HashMap<Integer, BiConsumer<ResponsePacket,JsonObject>> queue;
    private RequestQueue() {
        queue = new HashMap<>();
    }


    int packetNumber = 0;
    public void request(String endpoint, Object data, BiConsumer<ResponsePacket,JsonObject> packet) {
        ClientPlayNetworking.send(new RequestPacket(packetNumber,endpoint,AtmModInit.GSON.toJson(data.toString())));
        queue.put(packetNumber++, packet);
    }

    public void onResponse(ResponsePacket packet) {
        queue.getOrDefault(packet.id(),(p,jsonObject)->{
            AtmModInit.LOGGER.warn("Received wrong response id {} ",packet.id());
        }).accept(packet,AtmModInit.GSON.fromJson(packet.data(), JsonObject.class));
        queue.remove(packet.id());
    }
}
