package mekanism.api.inventory;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class ItemHandlerHelper {

    private ItemHandlerHelper() {
    }

    public static boolean canItemStacksStack(ItemStack first, ItemStack second) {
        return !first.isEmpty() && !second.isEmpty() && ItemStack.isSameItemSameTags(first, second);
    }

    public static ItemStack insertItemStacked(@Nullable IExtendedItemHandler inventory, ItemStack stack, boolean simulate) {
        if (inventory == null || stack.isEmpty()) {
            return stack;
        }
        ItemStack remainder = stack;
        int slots = inventory.getSlots();
        if (!stack.isStackable()) {
            for (int slot = 0; slot < slots && !remainder.isEmpty(); slot++) {
                remainder = inventory.insertItem(slot, remainder, simulate);
            }
            return remainder;
        }
        for (int pass = 0; pass < 2 && !remainder.isEmpty(); pass++) {
            for (int slot = 0; slot < slots && !remainder.isEmpty(); slot++) {
                ItemStack stored = inventory.getStackInSlot(slot);
                if (pass == 0 ? stored.isStackable() && canItemStacksStack(stored, remainder) : stored.isEmpty()) {
                    remainder = inventory.insertItem(slot, remainder, simulate);
                }
            }
        }
        return remainder;
    }
}
