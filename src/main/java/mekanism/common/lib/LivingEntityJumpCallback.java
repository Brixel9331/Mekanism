package mekanism.common.lib;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.LivingEntity;

@FunctionalInterface
public interface LivingEntityJumpCallback {

    Event<LivingEntityJumpCallback> EVENT = EventFactory.createArrayBacked(LivingEntityJumpCallback.class, listeners -> entity -> {
        for (LivingEntityJumpCallback listener : listeners) {
            listener.onJump(entity);
        }
    });

    void onJump(LivingEntity entity);
}
