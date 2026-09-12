package mekanism.api.inventory;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;

public interface ITransactionalInventorySlot extends IInventorySlot {

    ItemStack insertItem(ItemStack stack, TransactionContext transaction);

    ItemStack extractItem(int amount, TransactionContext transaction);
}
