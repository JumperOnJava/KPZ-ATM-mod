package io.github.jumperonjava.kpz_atm_mod;

import com.google.gson.Gson;
import io.github.jumperonjava.kpz_atm_mod.packets.OpenAtmS2CPacket;
import io.github.jumperonjava.kpz_atm_mod.packets.RequestPacket;
import io.github.jumperonjava.kpz_atm_mod.packets.ResponsePacket;
import net.fabricmc.api.ModInitializer;
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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
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

        RequestPacket.registerServerReceive();
    }

    public static String hashPassword(String password) {
        try {
            var md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
