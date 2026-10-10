package mekanism.common.item.interfaces;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface IItemStackProperties {

    default int getMaxDamage(ItemStack stack) {
        return ((Item) this).getMaxDamage();
    }

    default int getMaxStackSize(ItemStack stack) {
        return ((Item) this).getMaxStackSize();
    }

    default int getDefaultTooltipHideFlags(ItemStack stack) {
        return 0;
    }

    default boolean isNotReplaceableByPickAction(ItemStack stack, Player player, int inventorySlot) {
        return stack.isEnchanted();
    }
}
