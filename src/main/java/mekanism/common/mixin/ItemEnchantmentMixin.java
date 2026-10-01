package mekanism.common.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.Map;
import mekanism.common.item.interfaces.IItemEnchantments;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public abstract class ItemEnchantmentMixin {

    @Inject(method = "getItemEnchantmentLevel", at = @At("RETURN"), cancellable = true)
    private static void mekanism$effectiveLevel(Enchantment enchantment, ItemStack stack, CallbackInfoReturnable<Integer> callback) {
        if (!stack.isEmpty() && stack.getItem() instanceof IItemEnchantments provider) {
            callback.setReturnValue(provider.getEnchantmentLevel(stack, enchantment, callback.getReturnValueI()));
        }
    }

    @Redirect(method = "runIterationOnItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getEnchantmentTags()Lnet/minecraft/nbt/ListTag;"))
    private static ListTag mekanism$effectiveEnchantments(ItemStack stack) {
        ListTag storedTags = stack.getEnchantmentTags();
        if (!(stack.getItem() instanceof IItemEnchantments provider)) {
            return storedTags;
        }
        Map<Enchantment, Integer> enchantments = provider.getAllEnchantments(stack, EnchantmentHelper.deserializeEnchantments(storedTags));
        ListTag effectiveTags = new ListTag();
        enchantments.forEach((enchantment, level) -> effectiveTags.add(EnchantmentHelper.storeEnchantment(EnchantmentHelper.getEnchantmentId(enchantment), level)));
        return effectiveTags;
    }

    @ModifyExpressionValue(method = "getAvailableEnchantmentResults", at = @At(value = "INVOKE",
          target = "Lnet/minecraft/world/item/enchantment/EnchantmentCategory;canEnchant(Lnet/minecraft/world/item/Item;)Z"))
    private static boolean mekanism$tableEligibility(boolean original, int level, ItemStack stack, boolean allowTreasure,
          @Local Enchantment enchantment) {
        return stack.getItem() instanceof IItemEnchantments provider ? provider.canApplyAtEnchantingTable(stack, enchantment) : original;
    }
}
