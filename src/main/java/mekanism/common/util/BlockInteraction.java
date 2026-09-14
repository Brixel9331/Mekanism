package mekanism.common.util;

import mekanism.common.lib.EntityBlockPlaceCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class BlockInteraction {

    private BlockInteraction() {
    }

    public static boolean canBreak(Level world, Player player, BlockPos pos, BlockState state) {
        return world.isClientSide || PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(world, player, pos, state, world.getBlockEntity(pos));
    }

    public static boolean tryPlace(Level world, @Nullable Entity entity, BlockPos pos, Direction face, BlockState newState) {
        if (world.isClientSide || entity instanceof Player player && !world.mayInteract(player, pos)) {
            return false;
        }
        BlockState originalState = world.getBlockState(pos);
        return EntityBlockPlaceCallback.EVENT.invoker().canPlace(world, entity, pos, face, originalState, newState)
              && world.setBlockAndUpdate(pos, newState);
    }
}
