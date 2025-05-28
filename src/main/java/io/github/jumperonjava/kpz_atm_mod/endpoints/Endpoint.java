package io.github.jumperonjava.kpz_atm_mod.endpoints;

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

interface Endpoint {
    Object handle(ServerPlayNetworking.Context context, JsonObject body);
}
