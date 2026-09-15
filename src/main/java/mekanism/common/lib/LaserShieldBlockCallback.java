package mekanism.common.lib;

import java.util.Objects;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

@FunctionalInterface
public interface LaserShieldBlockCallback {

    Event<LaserShieldBlockCallback> EVENT = EventFactory.createArrayBacked(LaserShieldBlockCallback.class, listeners -> (entity, source, result) -> {
        for (LaserShieldBlockCallback listener : listeners) {
            if (result.blockedDamage() == 0) {
                break;
            }
            Result next = Objects.requireNonNull(listener.block(entity, source, result));
            result = new Result(Math.min(result.blockedDamage(), next.blockedDamage()), next.damageShield());
        }
        return result;
    });

    Result block(LivingEntity entity, DamageSource source, Result result);

    record Result(float blockedDamage, boolean damageShield) {

        public Result {
            if (!Float.isFinite(blockedDamage) || blockedDamage < 0) {
                throw new IllegalArgumentException("Blocked damage must be finite and non-negative");
            }
        }
    }
}
