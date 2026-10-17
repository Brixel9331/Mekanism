package mekanism.common.item.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;

public interface ISneakBypassUseItem {

    boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player);

    static boolean bypasses(ItemStack stack, LevelReader world, BlockPos pos, Player player) {
        return stack.isEmpty() || stack.getItem() instanceof ISneakBypassUseItem item && item.doesSneakBypassUse(stack, world, pos, player);
    }
}
