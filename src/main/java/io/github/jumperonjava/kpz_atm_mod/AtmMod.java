package io.github.jumperonjava.kpz_atm_mod;

import com.google.gson.Gson;
import io.github.jumperonjava.kpz_atm_mod.server.networking.RequestEndpointAdapter;
import io.github.jumperonjava.kpz_atm_mod.server.bank.DefaultBankEndpoints;
import io.github.jumperonjava.kpz_atm_mod.server.networking.ServerPacketQueue;
import io.github.jumperonjava.kpz_atm_mod.packets.OpenAtmS2CPacket;
import io.github.jumperonjava.kpz_atm_mod.packets.RequestPacket;
import io.github.jumperonjava.kpz_atm_mod.packets.ResponsePacket;
import io.github.jumperonjava.kpz_atm_mod.server.networking.ServerThreadExecutor;
import io.github.jumperonjava.kpz_atm_mod.server.bank.DatabaseBankService;
import io.github.jumperonjava.kpz_atm_mod.server.bank.DatabaseUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class AtmMod implements ModInitializer {

    public static final String MOD_ID = "kpz_atm_mod";
    public static Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static Gson GSON = new Gson();


    static Block Atm = Blocks.register(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(AtmMod.MOD_ID, "atm_block")), AtmBlock::new, AbstractBlock.Settings.create().hardness(1).lootTable(Optional.empty()));
    static Item AtmItem = Items.register(Atm);
    @Override
    public void onInitialize() {
        OpenAtmS2CPacket.register();
        RequestPacket.register();
        ResponsePacket.register();

        var database = new DatabaseUtil("root", "", "jdbc:mysql://localhost:3306/bankdatabase");
        var bank = new DatabaseBankService(database);
        var serverThreadExecutor = new ServerThreadExecutor();
        var endpoints = new DefaultBankEndpoints(bank, serverThreadExecutor);
        var endpointHandler = new RequestEndpointAdapter(endpoints);
        var serverQueue = new ServerPacketQueue(endpointHandler);


        ServerPlayNetworking.registerGlobalReceiver(RequestPacket.ID, serverQueue);
        ServerTickEvents.END_SERVER_TICK.register(serverThreadExecutor);
    }



}
