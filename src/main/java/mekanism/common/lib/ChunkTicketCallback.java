package mekanism.common.lib;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerLevel;

@FunctionalInterface
public interface ChunkTicketCallback {

    Event<ChunkTicketCallback> EVENT = EventFactory.createArrayBacked(ChunkTicketCallback.class, listeners -> (level, chunk, oldLevel, newLevel) -> {
        for (ChunkTicketCallback listener : listeners) {
            listener.changed(level, chunk, oldLevel, newLevel);
        }
    });

    void changed(ServerLevel level, long chunk, int oldLevel, int newLevel);
}
