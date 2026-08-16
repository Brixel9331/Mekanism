package mekanism.common.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import java.nio.file.Path;
import java.util.function.Function;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraftforge.fml.config.ConfigFileTypeHandler;
import net.minecraftforge.fml.config.ModConfig;

/**
 * Custom {@link ModConfig} implementation that allows for rerouting the server config from being in the world's folder to being in the normal config folder. This allows
 * for us to use the builtin sync support, without the extra hassle of having to explain to people where the config file is, or require people in single player to edit
 * the config each time they make a new world.
 */
public class MekanismModConfig extends ModConfig {

    private static final MekanismConfigFileTypeHandler MEK_TOML = new MekanismConfigFileTypeHandler();

    public MekanismModConfig(String modId, IMekanismConfig config) {
        super(config.getConfigType(), config.getConfigSpec(), modId, "Mekanism/" + config.getFileName() + ".toml");
    }

    @Override
    public ConfigFileTypeHandler getHandler() {
        return MEK_TOML;
    }

    private static class MekanismConfigFileTypeHandler extends ConfigFileTypeHandler {

        private static Path getPath(Path configBasePath, ModConfig config) {
            //Intercept server config path reading for Mekanism configs and reroute it to the normal config directory
            if (config.getType() == Type.SERVER) {
                return FabricLoader.getInstance().getConfigDir();
            }
            return configBasePath;
        }

        @Override
        public Function<ModConfig, CommentedFileConfig> reader(Path configBasePath) {
            return config -> super.reader(getPath(configBasePath, config)).apply(config);
        }

        @Override
        public void unload(Path configBasePath, ModConfig config) {
            super.unload(getPath(configBasePath, config), config);
        }
    }
}