package io.github.jumperonjava.kpz_atm_mod.server.endpoints;

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public interface Endpoint {
    Object handle(ServerPlayNetworking.Context context, JsonObject body) throws Exception;
}
