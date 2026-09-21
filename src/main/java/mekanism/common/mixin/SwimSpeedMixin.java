package mekanism.common.mixin;

import mekanism.common.registries.MekanismAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LivingEntity.class)
public abstract class SwimSpeedMixin {

    @ModifyArg(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;moveRelative(FLnet/minecraft/world/phys/Vec3;)V", ordinal = 0), index = 0)
    private float mekanism$swimSpeed(float speed) {
        return (Object) this instanceof Player player ? speed * (float) player.getAttributeValue(MekanismAttributes.SWIM_SPEED) : speed;
    }

    @ModifyArg(method = {"jumpInLiquid", "goDownInWater"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;"), index = 1)
    private double mekanism$verticalSwimSpeed(double speed) {
        return (Object) this instanceof Player player ? speed * player.getAttributeValue(MekanismAttributes.SWIM_SPEED) : speed;
    }
}
