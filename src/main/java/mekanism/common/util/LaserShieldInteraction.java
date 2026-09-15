package mekanism.common.util;

import mekanism.common.lib.LaserShieldBlockCallback;
import mekanism.common.lib.LaserShieldBlockCallback.Result;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class LaserShieldInteraction {

    private LaserShieldInteraction() {
    }

    /**
     * Based off of Player#hurtCurrentlyUsedShield
     */
    public static float block(LivingEntity entity, DamageSource source, Vec3 origin, float damage, int absorptionRatio) {
        if (absorptionRatio <= 0 || !Float.isFinite(damage) || damage < 0) {
            throw new IllegalArgumentException("Laser damage must be finite and non-negative and absorption ratio must be positive");
        }
        Level level = entity.level();
        if (level.isClientSide || damage == 0 || !entity.isBlocking()) {
            return 0;
        }
        Vec3 incoming = entity.position().subtract(origin).normalize();
        //Validate the player is facing the laser
        if (new Vec3(incoming.x, 0, incoming.z).dot(entity.getViewVector(1)) >= 0) {
            return 0;
        }
        Result result = LaserShieldBlockCallback.EVENT.invoker().block(entity, source, new Result(damage, true));
        float blocked = result.blockedDamage();
        if (blocked == 0) {
            return 0;
        }
        double effectiveDamage = (double) blocked / absorptionRatio;
        //Allow the shield to absorb sub single unit damage values for free
        if (result.damageShield() && effectiveDamage >= 1) {
            ItemStack shield = entity.getUseItem();
            int durabilityNeeded = (int) Math.min(Integer.MAX_VALUE - Math.max(0, shield.getDamageValue()), 1 + Math.floor(effectiveDamage));
            int durabilityAvailable = Math.max(0, shield.getMaxDamage() - shield.getDamageValue());
            InteractionHand hand = entity.getUsedItemHand();
            shield.hurtAndBreak(durabilityNeeded, entity, living -> living.broadcastBreakEvent(hand));
            if (shield.isEmpty()) {
                entity.setItemInHand(hand, ItemStack.EMPTY);
                entity.stopUsingItem();
                entity.playSound(SoundEvents.SHIELD_BREAK, 0.8F, 0.8F + 0.4F * level.random.nextFloat());
                double durabilityBlocked = durabilityAvailable - 1 + (effectiveDamage - Math.floor(effectiveDamage));
                blocked = (float) Math.min(blocked, Math.max(0, durabilityBlocked * absorptionRatio));
            }
        }
        if (entity instanceof ServerPlayer player && blocked > 0 && blocked < 3.4028235E37F) {
            player.awardStat(Stats.DAMAGE_BLOCKED_BY_SHIELD, Math.round(blocked * 10F));
        }
        return blocked;
    }
}
