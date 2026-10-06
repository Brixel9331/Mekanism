package mekanism.common.registration.impl;

import mekanism.common.registration.RegistryObject;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviderType;

public class IntProviderTypeRegistryObject<PROVIDER extends IntProvider> extends RegistryObject<IntProviderType<PROVIDER>> {

    public IntProviderTypeRegistryObject(RegistryObject<IntProviderType<PROVIDER>> registryObject) {
        super(registryObject);
    }
}