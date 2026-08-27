package mekanism.common.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import fuzs.forgeconfigapiport.api.config.v2.ModConfigEvents;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraftforge.fml.config.ConfigTracker;
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

    public static void prepareWorldgenConfig(IMekanismConfig config) {
        ModConfig tracked = ConfigTracker.INSTANCE.fileMap().get("Mekanism/" + config.getFileName() + ".toml");
        if (tracked != null && tracked.getSpec() == config.getConfigSpec() && tracked.getConfigData() != null) {
            return;
        }
        CommentedConfig snapshot = CommentedConfig.inMemory();
        Path path = CONFIG_DIR.resolve(config.getFileName() + ".toml");
        if (Files.exists(path)) {
            try (CommentedFileConfig file = CommentedFileConfig.of(path)) {
                file.load();
                snapshot.putAll(file);
            }
        }
        config.getConfigSpec().correct(snapshot);
        config.getConfigSpec().acceptConfig(snapshot);
        config.clearCache(false);
    }

    private static void clearCache(ModConfig loaded, IMekanismConfig config, boolean unloading) {
        if (loaded.getSpec() == config.getConfigSpec()) {
            config.clearCache(unloading);
        }
    }
}
