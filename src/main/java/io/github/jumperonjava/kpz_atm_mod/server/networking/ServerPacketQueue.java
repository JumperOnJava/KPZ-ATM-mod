package io.github.jumperonjava.kpz_atm_mod.server.networking;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.jumperonjava.kpz_atm_mod.AtmMod;
import io.github.jumperonjava.kpz_atm_mod.packets.RequestPacket;
import io.github.jumperonjava.kpz_atm_mod.packets.ResponsePacket;
import io.github.jumperonjava.kpz_atm_mod.server.Status;
import io.github.jumperonjava.kpz_atm_mod.server.bank.EndpointException;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import java.util.*;

public class ServerPacketQueue implements ServerPlayNetworking.PlayPayloadHandler<RequestPacket> {

    private final HashMap<String, RequestTypeHandler> endpoints = new HashMap<>();
    private Thread thread;

    public ServerPacketQueue(RequestHandlerProvider requestHandlerProvider) {
        this.endpoints.putAll(requestHandlerProvider.getRequestHandlers());
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
                RequestTypeHandler requestTypeHandler = endpoints.get(requestPacket.requestType());
                if (requestTypeHandler == null) {
                    throw new EndpointException(Status.ERROR, "Unknown requestType: " + requestPacket.requestType());
                }

                JsonObject requestBody = AtmMod.GSON.fromJson(requestPacket.data(), JsonObject.class);
                Object response = requestTypeHandler.handle(context, requestBody);

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