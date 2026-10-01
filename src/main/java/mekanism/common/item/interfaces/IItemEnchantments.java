package mekanism.common.item.interfaces;

import java.util.Map;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public interface IItemEnchantments {

    default int getEnchantmentLevel(ItemStack stack, Enchantment enchantment, int storedLevel) {
        return storedLevel;
    }

    default Map<Enchantment, Integer> getAllEnchantments(ItemStack stack, Map<Enchantment, Integer> storedEnchantments) {
        return storedEnchantments;
    }

    default boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment.category.canEnchant(stack.getItem());
    }

    default boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return true;
    }
}
