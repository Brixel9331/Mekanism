package mekanism.common.registration.impl;

import mekanism.common.registration.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class SoundEventDeferredRegister extends DeferredRegister<SoundEvent> {

    //We need to store the modid because the deferred register doesn't let you get the modid back out
    private final String modid;

    public SoundEventDeferredRegister(String modid) {
        super(modid, Registries.SOUND_EVENT, () -> BuiltInRegistries.SOUND_EVENT);
        this.modid = modid;
    }

    public SoundEventRegistryObject<SoundEvent> register(String name) {
        return register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(modid, name)), SoundEventRegistryObject::new);
    }
}