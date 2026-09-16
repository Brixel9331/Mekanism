package mekanism.common.mixin;

import mekanism.common.util.EntityTeleportation;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public abstract class EntityTeleportationMixin {

    @Redirect(method = "canChangeDimensions", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isPassenger()Z"))
    private boolean mekanism$checkPassengerTravel(Entity entity) {
        return !EntityTeleportation.isCheckingDimension(entity) && entity.isPassenger();
    }

    @Redirect(method = "canChangeDimensions", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isVehicle()Z"))
    private boolean mekanism$checkVehicleTravel(Entity entity) {
        return !EntityTeleportation.isCheckingDimension(entity) && entity.isVehicle();
    }
}
