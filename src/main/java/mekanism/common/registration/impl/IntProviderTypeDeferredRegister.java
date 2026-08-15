package mekanism.common.registration.impl;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import mekanism.common.registration.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviderType;

public class IntProviderTypeDeferredRegister extends DeferredRegister<IntProviderType<?>> {

    public IntProviderTypeDeferredRegister(String modid) {
        super(modid, Registries.INT_PROVIDER_TYPE, () -> BuiltInRegistries.INT_PROVIDER_TYPE);
    }

    public <PROVIDER extends IntProvider> IntProviderTypeRegistryObject<PROVIDER> register(String name, Codec<PROVIDER> codec) {
        return register(name, () -> () -> codec);
    }

    public <PROVIDER extends IntProvider> IntProviderTypeRegistryObject<PROVIDER> register(String name, Supplier<? extends IntProviderType<PROVIDER>> sup) {
        return register(name, sup, IntProviderTypeRegistryObject::new);
    }
}