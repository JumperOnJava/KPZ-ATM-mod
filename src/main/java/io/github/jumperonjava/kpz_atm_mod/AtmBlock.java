package io.github.jumperonjava.kpz_atm_mod;

import io.github.jumperonjava.kpz_atm_mod.packets.OpenAtmS2CPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AtmBlock extends Block {
    public AtmBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if(!world.isClient) {
            ServerPlayNetworking.send((ServerPlayerEntity) player, new OpenAtmS2CPacket());
        }

        return super.onUse(state, world, pos, player, hit);
    }
}
