package mekanism.common.registration.impl;

import java.util.function.Supplier;
import mekanism.common.registration.DeferredRegister;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;

public class ParticleTypeDeferredRegister extends DeferredRegister<ParticleType<?>> {

    public ParticleTypeDeferredRegister(String modid) {
        super(modid, Registries.PARTICLE_TYPE, () -> BuiltInRegistries.PARTICLE_TYPE);
    }

    public ParticleTypeRegistryObject<SimpleParticleType, SimpleParticleType> registerBasicParticle(String name) {
        return register(name, () -> FabricParticleTypes.simple());
    }

    public <PARTICLE extends ParticleOptions, TYPE extends ParticleType<PARTICLE>> ParticleTypeRegistryObject<PARTICLE, TYPE> register(String name, Supplier<TYPE> sup) {
        return register(name, sup, ParticleTypeRegistryObject::new);
    }
}