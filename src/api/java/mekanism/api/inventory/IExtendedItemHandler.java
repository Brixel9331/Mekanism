package mekanism.api.inventory;

import net.minecraft.world.item.ItemStack;

public interface IExtendedItemHandler {

    int getSlots();

    ItemStack getStackInSlot(int slot);

    void setStackInSlot(int slot, ItemStack stack);

    ItemStack insertItem(int slot, ItemStack stack, boolean simulate);

    ItemStack extractItem(int slot, int amount, boolean simulate);

    int getSlotLimit(int slot);

    boolean isItemValid(int slot, ItemStack stack);
}
