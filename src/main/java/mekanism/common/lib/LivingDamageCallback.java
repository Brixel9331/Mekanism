package mekanism.common.lib;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

@FunctionalInterface
public interface LivingDamageCallback {

    Event<LivingDamageCallback> EVENT = EventFactory.createArrayBacked(LivingDamageCallback.class, listeners -> (entity, source, amount) -> {
        for (LivingDamageCallback listener : listeners) {
            if (amount <= 0) {
                return 0;
            }
            amount = listener.modifyDamage(entity, source, amount);
        }
        return Math.max(0, amount);
    });

    float modifyDamage(LivingEntity entity, DamageSource source, float amount);
}
