package mekanism.common.lib;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface PlayerTickCallback {

    Event<PlayerTickCallback> EVENT = EventFactory.createArrayBacked(PlayerTickCallback.class, listeners -> player -> {
        for (PlayerTickCallback listener : listeners) {
            listener.onTick(player);
        }
    });

    void onTick(Player player);
}
