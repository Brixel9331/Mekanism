package mekanism.api.gear;

import net.minecraft.world.item.ItemStack;

public interface IToolActionItem {

    boolean canPerformAction(ItemStack stack, ToolAction action);
}
