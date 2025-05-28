package io.github.jumperonjava.kpz_atm_mod.endpoints;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.nimbusds.jwt.JWT;
import io.github.jumperonjava.kpz_atm_mod.AtmModInit;
import io.github.jumperonjava.kpz_atm_mod.packets.RequestPacket;
import io.github.jumperonjava.kpz_atm_mod.packets.ResponsePacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import java.util.HashMap;

public class Endpoints implements ServerPlayNetworking.PlayPayloadHandler<RequestPacket> {


    public static Endpoints getInstance() {
        if(instance == null) {
            instance = new Endpoints();
        }
        return instance;
    }

    private static Endpoints instance;



    HashMap<String, Endpoint> endpoints = new HashMap<>();

    private Endpoint LOGIN = this::login;

    private Object login(ServerPlayNetworking.Context context, JsonObject body) {
        if(body.get("username").getAsString().equals("admin") && body.get("password").getAsString().equals("admin")) {
            return new Object(){
                String token = "";
            };
        }
        else throw new EndpointException(Status.ERROR, "Invalid username or password");
    }
    private Endpoints(){
        endpoints.put("login", LOGIN);
    }


    @Override
    public void receive(RequestPacket requestPacket, ServerPlayNetworking.Context context) {
        new Thread(() -> {
            ResponsePacket responsePacket = null;
            try{
                Thread.sleep(1000);
                var response = endpoints
                    .get(requestPacket.endpoint())
                    .handle(
                            context,
                            AtmModInit.GSON.fromJson(requestPacket.data(), JsonObject.class));

                responsePacket = new ResponsePacket(requestPacket.id(), Status.SUCCESS, AtmModInit.GSON.toJson(response));

            }
            catch (EndpointException e) {
                var response = new JsonObject();
                response.add("error",new JsonPrimitive(e.message));
                responsePacket = new ResponsePacket(requestPacket.id(), Status.ERROR, AtmModInit.GSON.toJson(response));
            } catch (Exception e) {
                var response = new JsonObject();
                response.add("error",new JsonPrimitive(e.getMessage()));
                responsePacket = new ResponsePacket(requestPacket.id(), Status.ERROR_UNEXPECTED, AtmModInit.GSON.toJson(response));
            }
            ServerPlayNetworking.send(context.player(), responsePacket);
        }).start();
    }
}
