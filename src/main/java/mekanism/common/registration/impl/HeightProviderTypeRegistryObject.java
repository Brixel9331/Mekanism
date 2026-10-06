package mekanism.common.registration.impl;

import mekanism.common.registration.RegistryObject;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.HeightProviderType;

public class HeightProviderTypeRegistryObject<PROVIDER extends HeightProvider> extends RegistryObject<HeightProviderType<PROVIDER>> {

    public HeightProviderTypeRegistryObject(RegistryObject<HeightProviderType<PROVIDER>> registryObject) {
        super(registryObject);
    }
}