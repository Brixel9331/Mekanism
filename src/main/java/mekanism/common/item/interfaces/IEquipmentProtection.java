package mekanism.common.item.interfaces;

import java.util.function.Consumer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IEquipmentProtection {

    default boolean makesPiglinsNeutral(ItemStack stack, LivingEntity wearer) {
        return false;
    }

    default boolean isEnderMask(ItemStack stack, Player player, EnderMan enderman) {
        return false;
    }

    default boolean canWalkOnPowderedSnow(ItemStack stack, LivingEntity wearer) {
        return false;
    }

    default <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
        return amount;
    }
}
