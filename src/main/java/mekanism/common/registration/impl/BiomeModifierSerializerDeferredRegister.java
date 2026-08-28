package mekanism.common.registration.impl;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import mekanism.common.registration.DeferredRegister;
import mekanism.common.world.modifier.BiomeModifier;
import mekanism.common.world.modifier.WorldgenModifiers;

public class BiomeModifierSerializerDeferredRegister extends DeferredRegister<Codec<? extends BiomeModifier>> {

    public BiomeModifierSerializerDeferredRegister(String modid) {
        super(modid, WorldgenModifiers.BIOME_SERIALIZERS, WorldgenModifiers::biomeSerializers);
    }

    public <T extends BiomeModifier> BiomeModifierSerializerRegistryObject<T> register(String name, Supplier<Codec<T>> sup) {
        return register(name, sup, BiomeModifierSerializerRegistryObject::new);
    }
}
