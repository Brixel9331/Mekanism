package mekanism.common.item.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IBlockStartBreakItem {

    boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player);
}
