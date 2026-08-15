package mekanism.common.registration.impl;

import mekanism.common.registration.RegistryObject;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class FeatureRegistryObject<CONFIG extends FeatureConfiguration, FEATURE extends Feature<CONFIG>> extends RegistryObject<FEATURE> {

    public FeatureRegistryObject(RegistryObject<FEATURE> registryObject) {
        super(registryObject);
    }
}