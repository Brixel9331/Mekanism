package mekanism.additions.common.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mekanism.additions.common.item.ItemWalkieTalkie;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class WalkieTalkieReequipMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Unique
    private int mekanism$lastSlot = -1;

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;matches(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z", ordinal = 0))
    private boolean mekanism$mainHandAnimation(ItemStack previous, ItemStack current, Operation<Boolean> original) {
        return original.call(previous, current) || previous.getItem() instanceof ItemWalkieTalkie item &&
              !item.shouldCauseReequipAnimation(previous, current, mekanism$lastSlot != minecraft.player.getInventory().selected);
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;matches(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z", ordinal = 1))
    private boolean mekanism$offHandAnimation(ItemStack previous, ItemStack current, Operation<Boolean> original) {
        return original.call(previous, current) || previous.getItem() instanceof ItemWalkieTalkie item && !item.shouldCauseReequipAnimation(previous, current, false);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void mekanism$rememberSlot(CallbackInfo callback) {
        mekanism$lastSlot = minecraft.player.getInventory().selected;
    }
}
