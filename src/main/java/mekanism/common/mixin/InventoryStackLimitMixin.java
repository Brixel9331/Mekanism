package mekanism.common.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mekanism.common.item.interfaces.IItemStackProperties;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Inventory.class)
public abstract class InventoryStackLimitMixin {

    @WrapOperation(method = "addResource(ILnet/minecraft/world/item/ItemStack;)I", at = @At(value = "INVOKE",
          target = "Lnet/minecraft/world/item/ItemStack;getMaxStackSize()I"))
    private int mekanism$insertionLimit(ItemStack destination, Operation<Integer> original, int slot, ItemStack source) {
        return destination.isEmpty() && source.getItem() instanceof IItemStackProperties ? source.getMaxStackSize() : original.call(destination);
    }
}
