package io.github.jumperonjava.kpz_atm_mod.packets;

import io.github.jumperonjava.kpz_atm_mod.AtmMod;
import io.github.jumperonjava.kpz_atm_mod.endpoints.Endpoints;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record RequestPacket(int id, String endpoint, String data) implements CustomPayload {
        public static final Identifier PACKET_ID = Identifier.of(AtmMod.MOD_ID, "request");
        public static final Id<RequestPacket> ID = new Id<>(PACKET_ID);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }

        public static void register() {
                PayloadTypeRegistry.playC2S().register(RequestPacket.ID, new PacketCodec<>() {
                    @Override
                    public RequestPacket decode(RegistryByteBuf buf) {
                        int id = buf.readInt();
                        String endpoint = buf.readString();
                        String data = buf.readString();
                        return new RequestPacket(id, endpoint, data);
                    }

                    @Override
                    public void encode(RegistryByteBuf buf, RequestPacket value) {
                        buf.writeInt(value.id);
                        buf.writeString(value.endpoint);
                        buf.writeString(value.data);
                    }
                });
        }

        public static void registerServerReceive() {
                ServerPlayNetworking.registerGlobalReceiver(RequestPacket.ID, Endpoints.getInstance());
        }


}
