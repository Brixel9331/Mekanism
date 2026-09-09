package mekanism.common.lib;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

public final class ServerLifecycle {

    private static volatile MinecraftServer currentServer;
    private static boolean registered;

    private ServerLifecycle() {
    }

    public static synchronized void register() {
        if (!registered) {
            ServerLifecycleEvents.SERVER_STARTING.register(server -> currentServer = server);
            ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
                if (currentServer == server) {
                    currentServer = null;
                }
            });
            registered = true;
        }
    }

    @Nullable
    public static MinecraftServer getCurrentServer() {
        return currentServer;
    }

    public static boolean isServerThread() {
        MinecraftServer server = currentServer;
        return server != null && server.isSameThread();
    }
}
