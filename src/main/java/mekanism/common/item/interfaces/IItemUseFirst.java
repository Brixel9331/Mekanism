package mekanism.common.item.interfaces;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public interface IItemUseFirst {

    InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context);
}
