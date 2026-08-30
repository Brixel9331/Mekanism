package mekanism.common.mixin;

import mekanism.common.lib.LivingDamageCallback;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LivingEntity.class, Player.class})
public abstract class LivingDamageMixin {

    @ModifyVariable(method = "actuallyHurt", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float mekanism$modifyDamage(float amount, DamageSource source, float originalAmount) {
        LivingEntity entity = (LivingEntity) (Object) this;
        return entity.isInvulnerableTo(source) ? amount : LivingDamageCallback.EVENT.invoker().modifyDamage(entity, source, amount);
    }

    @Inject(method = "actuallyHurt", at = {
          @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getDamageAfterArmorAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F"),
          @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getDamageAfterArmorAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F")
    }, cancellable = true)
    private void mekanism$cancelAbsorbedDamage(DamageSource source, float amount, CallbackInfo ci) {
        if (amount <= 0) {
            ci.cancel();
        }
    }
}
