package mekanism.api.fluid;

import net.minecraft.world.item.ItemStack;

public interface IExtendedFluidHandlerItem extends IExtendedFluidHandler {

    ItemStack getContainer();
}
