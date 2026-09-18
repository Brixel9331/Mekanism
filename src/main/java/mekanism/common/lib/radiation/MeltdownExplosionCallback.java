package mekanism.common.lib.radiation;

import mekanism.common.lib.radiation.Meltdown.MeltdownExplosion;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerLevel;

@FunctionalInterface
public interface MeltdownExplosionCallback {

    Event<MeltdownExplosionCallback> EVENT = EventFactory.createArrayBacked(MeltdownExplosionCallback.class, listeners -> (world, explosion) -> {
        for (MeltdownExplosionCallback listener : listeners) {
            if (!listener.canExplode(world, explosion)) {
                return false;
            }
        }
        return true;
    });

    boolean canExplode(ServerLevel world, MeltdownExplosion explosion);
}
