package mekanism.common.item.interfaces;

import net.fabricmc.fabric.api.entity.event.v1.FabricElytraItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface ICustomElytraItem extends FabricElytraItem {

    boolean canElytraFly(ItemStack stack, LivingEntity entity);

    boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks);

    @Override
    default boolean useCustomElytra(LivingEntity entity, ItemStack stack, boolean tickElytra) {
        return canElytraFly(stack, entity) && (!tickElytra || elytraFlightTick(stack, entity, entity.getFallFlyingTicks()));
    }
}
