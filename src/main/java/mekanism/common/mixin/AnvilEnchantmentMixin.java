package mekanism.common.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mekanism.common.item.interfaces.IItemEnchantments;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AnvilMenu.class)
public abstract class AnvilEnchantmentMixin extends ItemCombinerMenu {

    protected AnvilEnchantmentMixin(MenuType<?> type, int id, Inventory inventory, ContainerLevelAccess access) {
        super(type, id, inventory, access);
    }

    @WrapOperation(method = "createResult", at = @At(value = "INVOKE",
          target = "Lnet/minecraft/world/item/enchantment/Enchantment;canEnchant(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean mekanism$enchantmentEligibility(Enchantment enchantment, ItemStack stack, Operation<Boolean> original) {
        return original.call(enchantment, stack) && (!(stack.getItem() instanceof IItemEnchantments provider) ||
              provider.canApplyAtEnchantingTable(stack, enchantment));
    }

    @ModifyArg(method = "createResult", at = @At(value = "INVOKE",
          target = "Lnet/minecraft/world/inventory/ResultContainer;setItem(ILnet/minecraft/world/item/ItemStack;)V"), index = 1)
    private ItemStack mekanism$bookEligibility(ItemStack result) {
        if (!result.isEmpty() && result.getItem() instanceof IItemEnchantments provider) {
            ItemStack book = inputSlots.getItem(1);
            if (book.is(Items.ENCHANTED_BOOK) && !EnchantedBookItem.getEnchantments(book).isEmpty() && !provider.isBookEnchantable(result, book)) {
                return ItemStack.EMPTY;
            }
        }
        return result;
    }
}
