package mekanism.common.lib;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.LivingEntity;

@FunctionalInterface
public interface LivingEntityTickCallback {

    Event<LivingEntityTickCallback> EVENT = EventFactory.createArrayBacked(LivingEntityTickCallback.class, listeners -> entity -> {
        for (LivingEntityTickCallback listener : listeners) {
            listener.onTick(entity);
        }
    });

    void onTick(LivingEntity entity);
}
