package mekanism.common.mixin;

import com.google.gson.JsonElement;
import mekanism.common.item.predicate.CustomItemPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemPredicate.class)
public abstract class ItemPredicateMixin {

    @Inject(method = "fromJson", at = @At("HEAD"), cancellable = true)
    private static void mekanism$customPredicate(JsonElement json, CallbackInfoReturnable<ItemPredicate> cir) {
        if (json != null && json.isJsonObject()) {
            ItemPredicate predicate = CustomItemPredicate.deserialize(json.getAsJsonObject());
            if (predicate != null) {
                cir.setReturnValue(predicate);
            }
        }
    }
}
