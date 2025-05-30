package io.github.jumperonjava.kpz_atm_mod.packets;

import io.github.jumperonjava.kpz_atm_mod.AtmMod;
import io.github.jumperonjava.kpz_atm_mod.client.RequestQueue;
import io.github.jumperonjava.kpz_atm_mod.client.ui.AtmScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record OpenAtmS2CPacket() implements CustomPayload {
        public static final Identifier PACKET_ID = Identifier.of(AtmMod.MOD_ID, "open_atm_screen");
        public static final CustomPayload.Id<OpenAtmS2CPacket> ID = new CustomPayload.Id<>(PACKET_ID);

        public static void register() {
                PayloadTypeRegistry.playS2C().register(OpenAtmS2CPacket.ID, new PacketCodec<>() {
                        @Override
                        public OpenAtmS2CPacket decode(RegistryByteBuf buf) {
                                return new OpenAtmS2CPacket();
                        }

                        @Override
                        public void encode(RegistryByteBuf buf, OpenAtmS2CPacket value) {
                        }
                });
        }

        public static void registerClientReceive(RequestQueue queue) {
                ClientPlayNetworking.registerGlobalReceiver(OpenAtmS2CPacket.ID, (payload, context) -> {
                        MinecraftClient.getInstance().setScreen(new AtmScreen(queue));
                });
        }

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
}
