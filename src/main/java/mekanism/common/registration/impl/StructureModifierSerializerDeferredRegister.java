package mekanism.common.registration.impl;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import mekanism.common.registration.DeferredRegister;
import mekanism.common.world.modifier.StructureModifier;
import mekanism.common.world.modifier.WorldgenModifiers;

public class StructureModifierSerializerDeferredRegister extends DeferredRegister<Codec<? extends StructureModifier>> {

    public StructureModifierSerializerDeferredRegister(String modid) {
        super(modid, WorldgenModifiers.STRUCTURE_SERIALIZERS, WorldgenModifiers::structureSerializers);
    }

    public <T extends StructureModifier> StructureModifierSerializerRegistryObject<T> register(String name, Supplier<Codec<T>> sup) {
        return register(name, sup, StructureModifierSerializerRegistryObject::new);
    }
}
