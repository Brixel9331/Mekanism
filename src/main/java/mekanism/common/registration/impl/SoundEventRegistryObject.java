package mekanism.common.registration.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.ILangEntry;
import mekanism.common.registration.RegistryObject;
import net.minecraft.Util;
import net.minecraft.sounds.SoundEvent;

@NothingNullByDefault
public class SoundEventRegistryObject<SOUND extends SoundEvent> extends RegistryObject<SOUND> implements ILangEntry {

    private final String translationKey;

    public SoundEventRegistryObject(RegistryObject<SOUND> registryObject) {
        super(registryObject);
        translationKey = Util.makeDescriptionId("sound_event", getId());
    }

    @Override
    public String getTranslationKey() {
        return translationKey;
    }
}