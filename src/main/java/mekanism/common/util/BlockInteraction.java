package mekanism.common.util;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockInteraction {

    private BlockInteraction() {
    }

    public static boolean canBreak(Level world, Player player, BlockPos pos, BlockState state) {
        return world.isClientSide || PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(world, player, pos, state, world.getBlockEntity(pos));
    }
}
