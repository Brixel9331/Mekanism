package mekanism.common.registration.impl;

import mekanism.common.registration.RegistryObject;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;

public class ParticleTypeRegistryObject<PARTICLE extends ParticleOptions, TYPE extends ParticleType<PARTICLE>> extends RegistryObject<TYPE> {

    public ParticleTypeRegistryObject(RegistryObject<TYPE> registryObject) {
        super(registryObject);
    }
}