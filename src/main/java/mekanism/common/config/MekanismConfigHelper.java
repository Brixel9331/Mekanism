package mekanism.common.config;

import fuzs.forgeconfigapiport.api.config.v2.ModConfigEvents;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraftforge.fml.config.ModConfig;

public class MekanismConfigHelper {

    private MekanismConfigHelper() {
    }

    public static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("Mekanism");

    /**
     * Creates a mod config so that {@link net.minecraftforge.fml.config.ConfigTracker} will track it and sync server configs from server to client.
     */
    public static MekanismModConfig registerConfig(String modId, IMekanismConfig config) {
        ModConfigEvents.loading(modId).register(loaded -> clearCache(loaded, config, false));
        ModConfigEvents.reloading(modId).register(loaded -> clearCache(loaded, config, false));
        ModConfigEvents.unloading(modId).register(loaded -> clearCache(loaded, config, true));
        return new MekanismModConfig(modId, config);
    }

    private static void clearCache(ModConfig loaded, IMekanismConfig config, boolean unloading) {
        if (loaded.getSpec() == config.getConfigSpec()) {
            config.clearCache(unloading);
        }
    }
}
