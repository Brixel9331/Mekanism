package mekanism.common.registration.impl;

import com.mojang.serialization.Codec;
import mekanism.common.registration.RegistryObject;
import mekanism.common.world.modifier.BiomeModifier;

public class BiomeModifierSerializerRegistryObject<T extends BiomeModifier> extends RegistryObject<Codec<T>> {

    public BiomeModifierSerializerRegistryObject(RegistryObject<Codec<T>> registryObject) {
        super(registryObject);
    }
}
