package mekanism.common.util;

import mekanism.common.tile.interfaces.IBoundingBlockRemoval;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class BoundingBlockInteraction {

    private BoundingBlockInteraction() {
    }

    public static ItemStack pickMainBlock(BlockGetter world, @Nullable BlockPos mainPos) {
        if (mainPos == null) {
            return ItemStack.EMPTY;
        }
        BlockState mainState = world.getBlockState(mainPos);
        return mainState.getBlock().getCloneItemStack(world, mainPos, mainState);
    }

    public static void removeMainBlock(Level world, BlockPos mainPos, BlockState mainState, @Nullable BlockEntity boundingTile) {
        IBoundingBlockRemoval removal = boundingTile instanceof IBoundingBlockRemoval proxy ? proxy : null;
        if (removal != null) {
            removal.setRemovedMainBlock(null);
        }
        if (!mainState.isAir()) {
            BlockEntity mainTile = world.getBlockEntity(mainPos);
            if (world.removeBlock(mainPos, false) && removal != null) {
                removal.setRemovedMainBlock(new RemovedMainBlock(mainPos, mainState, mainTile));
            }
        }
    }

    public static boolean playerDestroy(Level world, Player player, @Nullable BlockEntity boundingTile, ItemStack tool) {
        if (boundingTile instanceof IBoundingBlockRemoval removal) {
            RemovedMainBlock mainBlock = removal.takeRemovedMainBlock();
            if (mainBlock != null) {
                mainBlock.state().getBlock().playerDestroy(world, player, mainBlock.pos(), mainBlock.state(), mainBlock.blockEntity(), tool);
                return true;
            }
        }
        return false;
    }

    public record RemovedMainBlock(BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity) {

        public RemovedMainBlock {
            pos = pos.immutable();
        }
    }
}
