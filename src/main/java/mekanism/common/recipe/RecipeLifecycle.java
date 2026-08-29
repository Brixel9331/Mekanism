package mekanism.common.recipe;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

public final class RecipeLifecycle {

    public static final Event<ServerRecipesUpdated> SERVER_UPDATED = EventFactory.createArrayBacked(ServerRecipesUpdated.class,
          listeners -> (server, initial) -> {
              for (ServerRecipesUpdated listener : listeners) {
                  listener.onRecipesUpdated(server, initial);
              }
          });
    public static final Event<Runnable> CLIENT_UPDATED = EventFactory.createArrayBacked(Runnable.class, listeners -> () -> {
        for (Runnable listener : listeners) {
            listener.run();
        }
    });

    private static boolean registered;

    private RecipeLifecycle() {
    }

    public static void register() {
        if (!registered) {
            ServerLifecycleEvents.SERVER_STARTED.register(server -> SERVER_UPDATED.invoker().onRecipesUpdated(server, true));
            ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resources, success) -> {
                if (success) {
                    SERVER_UPDATED.invoker().onRecipesUpdated(server, false);
                }
            });
            registered = true;
        }
    }

    @FunctionalInterface
    public interface ServerRecipesUpdated {

        void onRecipesUpdated(MinecraftServer server, boolean initial);
    }
}
