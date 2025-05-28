package io.github.jumperonjava.kpz_atm_mod.endpoints;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.nimbusds.jwt.JWT;
import io.github.jumperonjava.kpz_atm_mod.AtmModInit;
import io.github.jumperonjava.kpz_atm_mod.packets.RequestPacket;
import io.github.jumperonjava.kpz_atm_mod.packets.ResponsePacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import java.util.HashMap;

public class Endpoints {


    static HashMap<String, Endpoint> endpoints = new HashMap<>();

    public static Endpoint LOGIN = Endpoints::login;

    private static Object login(ServerPlayNetworking.Context context, JsonObject body) {
        if(body.get("username").getAsString().equals("admin") && body.get("password").getAsString().equals("admin")) {
            return new Object(){
                String token = "";
            };
        }
        else throw new EndpointException(Status.ERROR, "Invalid username or password");
    }
    static {
        endpoints.put("login", LOGIN);
    }


    public static void handleRequests(RequestPacket requestPacket, ServerPlayNetworking.Context context) {
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
