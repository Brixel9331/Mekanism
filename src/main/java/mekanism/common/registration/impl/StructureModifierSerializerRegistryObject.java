package mekanism.common.registration.impl;

import com.mojang.serialization.Codec;
import mekanism.common.registration.RegistryObject;
import mekanism.common.world.modifier.StructureModifier;

public class StructureModifierSerializerRegistryObject<T extends StructureModifier> extends RegistryObject<Codec<T>> {

    public StructureModifierSerializerRegistryObject(RegistryObject<Codec<T>> registryObject) {
        super(registryObject);
    }
}
