package io.github.jumperonjava.kpz_atm_mod.packets;

import io.github.jumperonjava.kpz_atm_mod.AtmModInit;
import io.github.jumperonjava.kpz_atm_mod.client.RequestQueue;
import io.github.jumperonjava.kpz_atm_mod.endpoints.Status;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ResponsePacket(int id, Status status, String data) implements CustomPayload {
        public static final Identifier PACKET_ID = Identifier.of(AtmModInit.MOD_ID, "request");
        public static final Id<ResponsePacket> ID = new Id<>(PACKET_ID);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }

        public static void register() {
                PayloadTypeRegistry.playS2C().register(ResponsePacket.ID, new PacketCodec<>() {
                    @Override
                    public ResponsePacket decode(RegistryByteBuf buf) {
                        int id = buf.readInt();
                        Status status = buf.readEnumConstant(Status.class);
                        String data = buf.readString();
                        return new ResponsePacket(id, status, data);
                    }

                    @Override
                    public void encode(RegistryByteBuf buf, ResponsePacket value) {
                        buf.writeInt(value.id);
                        buf.writeEnumConstant(value.status);
                        buf.writeString(value.data);
                    }
                });
        }

        public static void registerClientReceive() {
            ClientPlayNetworking.registerGlobalReceiver(ResponsePacket.ID, (payload, context) -> {
                RequestQueue.getInstance().onResponse(payload);
            });
        }

}
