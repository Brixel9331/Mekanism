package mekanism.common.mixin;

import mekanism.common.registries.MekanismAttributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerAttributesMixin {

    @Inject(method = "createAttributes", at = @At("RETURN"))
    private static void mekanism$movementAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> callback) {
        callback.getReturnValue().add(MekanismAttributes.STEP_HEIGHT).add(MekanismAttributes.SWIM_SPEED);
    }
}
