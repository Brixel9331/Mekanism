package mekanism.common.util;

import mekanism.api.gear.IToolActionItem;
import mekanism.api.gear.ToolAction;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class ToolModification {

    private ToolModification() {
    }

    public static boolean canPerform(ItemStack stack, ToolAction action) {
        return stack.getItem() instanceof IToolActionItem item && item.canPerformAction(stack, action);
    }

    @Nullable
    public static BlockState getModifiedState(BlockState state, UseOnContext context, ToolAction action, boolean simulate) {
        if (!canPerform(context.getItemInHand(), action)) {
            return null;
        }
        if (action == ToolAction.AXE_STRIP) {
            Block stripped = AxeItem.STRIPPABLES.get(state.getBlock());
            return stripped == null ? null : stripped.defaultBlockState().setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS));
        } else if (action == ToolAction.AXE_SCRAPE) {
            return WeatheringCopper.getPrevious(state).orElse(null);
        } else if (action == ToolAction.AXE_WAX_OFF) {
            Block unwaxed = HoneycombItem.WAX_OFF_BY_BLOCK.get().get(state.getBlock());
            return unwaxed == null ? null : unwaxed.withPropertiesOf(state);
        } else if (action == ToolAction.SHOVEL_FLATTEN) {
            return ShovelItem.FLATTENABLES.get(state.getBlock());
        } else if (action == ToolAction.HOE_TILL) {
            if (state.is(Blocks.ROOTED_DIRT)) {
                if (!simulate && !context.getLevel().isClientSide) {
                    Block.popResourceFromFace(context.getLevel(), context.getClickedPos(), context.getClickedFace(), new ItemStack(Items.HANGING_ROOTS));
                }
                return Blocks.DIRT.defaultBlockState();
            } else if (context.getLevel().isEmptyBlock(context.getClickedPos().above())) {
                if (state.is(Blocks.COARSE_DIRT)) {
                    return Blocks.DIRT.defaultBlockState();
                } else if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT_PATH) || state.is(Blocks.DIRT)) {
                    return Blocks.FARMLAND.defaultBlockState();
                }
            }
        }
        return null;
    }
}
