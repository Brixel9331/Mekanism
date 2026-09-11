package mekanism.common.util;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockHarvesting {

    private BlockHarvesting() {
    }

    public static boolean tryHarvest(ServerLevel world, ServerPlayer player, BlockPos pos, BlockState state, ItemStack tool) {
        Block block = state.getBlock();
        if (state.isAir() || state.getDestroySpeed(world, pos) < 0 || !world.mayInteract(player, pos)
              || !tool.getItem().canAttackBlock(state, world, pos, player)
              || block instanceof GameMasterBlock && !player.canUseGameMasterBlocks()
              || player.blockActionRestricted(world, pos, player.gameMode.getGameModeForPlayer())) {
            return false;
        }
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(world, player, pos, state, blockEntity)) {
            PlayerBlockBreakEvents.CANCELED.invoker().onBlockBreakCanceled(world, player, pos, state, blockEntity);
            return false;
        }
        block.playerWillDestroy(world, pos, state, player);
        if (!world.removeBlock(pos, false)) {
            return false;
        }
        PlayerBlockBreakEvents.AFTER.invoker().afterBlockBreak(world, player, pos, state, blockEntity);
        block.destroy(world, pos, state);
        if (!player.isCreative() && player.hasCorrectToolForDrops(state)) {
            block.playerDestroy(world, player, pos, state, blockEntity, tool);
        }
        return true;
    }
}
