package mekanism.common.mixin;

import mekanism.api.radiation.capability.IRadiationEntity;
import mekanism.common.lib.radiation.capability.DefaultRadiationEntity;
import mekanism.common.lib.radiation.capability.RadiationEntityAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityRadiationMixin implements RadiationEntityAccess {

    @Unique
    private final IRadiationEntity mekanism$radiation = new DefaultRadiationEntity();

    @Override
    public IRadiationEntity mekanism$getRadiation() {
        return mekanism$radiation;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void mekanism$saveRadiation(CompoundTag tag, CallbackInfo callback) {
        mekanism$writeRadiation(tag);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void mekanism$loadRadiation(CompoundTag tag, CallbackInfo callback) {
        mekanism$readRadiation(tag);
    }
}
