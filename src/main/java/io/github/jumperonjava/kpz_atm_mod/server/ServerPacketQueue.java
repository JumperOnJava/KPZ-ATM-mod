package io.github.jumperonjava.kpz_atm_mod.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.jumperonjava.kpz_atm_mod.AtmMod;
import io.github.jumperonjava.kpz_atm_mod.packets.RequestPacket;
import io.github.jumperonjava.kpz_atm_mod.packets.ResponsePacket;
import io.github.jumperonjava.kpz_atm_mod.server.endpoints.Endpoint;
import io.github.jumperonjava.kpz_atm_mod.server.endpoints.EndpointException;
import io.github.jumperonjava.kpz_atm_mod.server.endpoints.EndpointProvider;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import java.util.*;

public class ServerPacketQueue implements ServerPlayNetworking.PlayPayloadHandler<RequestPacket> {

    private final HashMap<String, Endpoint> endpoints = new HashMap<>();
    private Thread thread;

    public ServerPacketQueue(EndpointProvider endpoints) {
        this.endpoints.putAll(endpoints.getEndpoints());
        this.thread = new Thread(this::handleRequestPacket);
    }

    @Override
    public void receive(RequestPacket requestPacket, ServerPlayNetworking.Context context) {
        handleThread.add(new PacketHandler(requestPacket, context));
        if(!thread.isAlive()) {
            thread.start();
        }
    }

    Queue<Runnable> handleThread = new LinkedList<>();

    private void handleRequestPacket() {
        while (true) {
            if (handleThread.isEmpty()) {
                Thread.yield();
                continue;
            }
            handleThread.poll().run();
        }
    }


    private class PacketHandler implements Runnable {
        private final RequestPacket requestPacket;
        private final ServerPlayNetworking.Context context;

        public PacketHandler(RequestPacket requestPacket, ServerPlayNetworking.Context context) {
            this.requestPacket = requestPacket;
            this.context = context;
        }

        @Override
        public void run() {
            ResponsePacket responsePacket;
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
            ServerPlayNetworking.send(context.player(), responsePacket);
        }
    }
}