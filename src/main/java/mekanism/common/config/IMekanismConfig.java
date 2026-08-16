package mekanism.common.config;

import mekanism.common.config.value.CachedValue;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

public interface IMekanismConfig {

    String getFileName();

    ForgeConfigSpec getConfigSpec();

    default boolean isLoaded() {
        return getConfigSpec().isLoaded();
    }

    ModConfig.Type getConfigType();

    default void save() {
        getConfigSpec().save();
    }

    void clearCache(boolean unloading);

    void addCachedValue(CachedValue<?> configValue);
}