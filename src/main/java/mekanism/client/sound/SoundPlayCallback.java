package mekanism.client.sound;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface SoundPlayCallback {

    ResourceLocation TRACKING_PHASE = new ResourceLocation("mekanism", "sound_tracking");
    Event<SoundPlayCallback> EVENT = EventFactory.createWithPhases(SoundPlayCallback.class, listeners -> (engine, original, sound) -> {
        for (SoundPlayCallback listener : listeners) {
            sound = listener.onPlaySound(engine, original, sound);
        }
        return sound;
    }, Event.DEFAULT_PHASE, TRACKING_PHASE);

    @Nullable
    SoundInstance onPlaySound(SoundEngine engine, SoundInstance original, @Nullable SoundInstance sound);
}
