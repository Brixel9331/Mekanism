package mekanism.common.util;

import com.mojang.authlib.GameProfile;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import mekanism.api.MekanismAPI;
import mekanism.common.lib.ServerLifecycle;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.GameProfileCache;
import org.jetbrains.annotations.Nullable;

public final class UsernameLookup {

    private static final Set<UUID> warnedFails = ConcurrentHashMap.newKeySet();

    private UsernameLookup() {
    }

    public static String getLastKnownUsername(@Nullable UUID uuid) {
        if (uuid == null) {
            return "<???>";
        }
        MinecraftServer server = ServerLifecycle.getCurrentServer();
        if (server != null) {
            GameProfileCache cache = server.getProfileCache();
            if (cache != null) {
                Optional<GameProfile> profile = cache.get(uuid);
                if (profile.isPresent()) {
                    warnedFails.remove(uuid);
                    return profile.get().getName();
                }
            }
        }
        if (warnedFails.add(uuid)) {
            MekanismAPI.logger.warn("Failed to retrieve username for UUID {}, you might want to add it to the JSON cache", uuid);
        }
        return "<" + uuid + ">";
    }
}
