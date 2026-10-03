package mekanism.common.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import mekanism.common.item.interfaces.IItemStackProperties;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Inventory.class)
public abstract class InventoryPickMixin {

    @Shadow
    @Final
    public Player player;

    @WrapOperation(method = "getSuitableHotbarSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEnchanted()Z"))
    private boolean mekanism$protectEquipment(ItemStack stack, Operation<Boolean> original, @Local(ordinal = 1) int slot) {
        return stack.getItem() instanceof IItemStackProperties provider ? provider.isNotReplaceableByPickAction(stack, player, slot) : original.call(stack);
    }
}
