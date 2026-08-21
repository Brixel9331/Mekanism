package mekanism.common.mixin;

import mekanism.api.capabilities.CapabilityProvider;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityCapabilityMixin {

    @Inject(method = "setRemoved", at = @At("TAIL"))
    private void mekanism$invalidateCapabilities(Entity.RemovalReason reason, CallbackInfo callback) {
        if (this instanceof CapabilityProvider provider) {
            provider.invalidateCapabilities();
        }
    }
}
