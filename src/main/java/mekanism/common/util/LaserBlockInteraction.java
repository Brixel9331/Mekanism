package mekanism.common.util;

import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public final class LaserBlockInteraction {

    private LaserBlockInteraction() {
    }

    public static boolean canIgnite(BlockState state) {
        if (!(state.getBlock() instanceof TntBlock)) {
            return false;
        }
        FlammableBlockRegistry.Entry entry = FlammableBlockRegistry.getDefaultInstance().get(state.getBlock());
        return state.ignitedByLava() || entry != null && entry.getBurnChance() > 0;
    }

    public static boolean ignite(ServerLevel world, Player player, BlockPos pos, BlockState state) {
        if (!canIgnite(state) || !canBreak(world, player, pos, state)) {
            return false;
        }
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!world.removeBlock(pos, false)) {
            return false;
        }
        //Convert TNT that can be lit on fire into a tnt entity
        //Note: We don't mark the fake player as the igniter as then when the tnt explodes if it hits a player
        // there will be a crash as our fake player's level will be null
        TntBlock.explode(world, pos, null);
        PlayerBlockBreakEvents.AFTER.invoker().afterBlockBreak(world, player, pos, state, blockEntity);
        return true;
    }

    public static boolean harvest(ServerLevel world, Player player, BlockPos pos, BlockState state, ItemStack tool, Consumer<ItemStack> dropConsumer) {
        if (!canBreak(world, player, pos, state)) {
            return false;
        }
        BlockEntity blockEntity = world.getBlockEntity(pos);
        List<ItemStack> drops = Block.getDrops(state, world, pos, blockEntity, player, tool);
        if (!world.removeBlock(pos, false)) {
            return false;
        }
        drops.forEach(dropConsumer);
        state.spawnAfterBreak(world, pos, tool, false);
        PlayerBlockBreakEvents.AFTER.invoker().afterBlockBreak(world, player, pos, state, blockEntity);
        //TODO: We may want to evaluate at some point doing this with our fake player so that it is fired as the "cause"?
        world.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(null, state));
        world.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
        return true;
    }

    private static boolean canBreak(ServerLevel world, Player player, BlockPos pos, BlockState state) {
        if (world.isClientSide || state.isAir() || state.getDestroySpeed(world, pos) < 0 || !world.mayInteract(player, pos)
              || !state.equals(world.getBlockState(pos))) {
            return false;
        }
        if (!BlockInteraction.canBreak(world, player, pos, state)) {
            PlayerBlockBreakEvents.CANCELED.invoker().onBlockBreakCanceled(world, player, pos, state, world.getBlockEntity(pos));
            return false;
        }
        return state.equals(world.getBlockState(pos));
    }
}
