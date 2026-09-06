package mekanism.common.mixin.client;

import mekanism.client.sound.SoundPlayCallback;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {

    @Shadow
    private boolean loaded;

    @ModifyVariable(method = "play", at = @At("HEAD"), argsOnly = true)
    private SoundInstance mekanism$filterSound(SoundInstance sound) {
        return loaded ? SoundPlayCallback.EVENT.invoker().onPlaySound((SoundEngine) (Object) this, sound, sound) : sound;
    }

    @Inject(method = "play", at = @At(value = "FIELD", target = "Lnet/minecraft/client/sounds/SoundEngine;loaded:Z"), cancellable = true)
    private void mekanism$cancelSound(SoundInstance sound, CallbackInfo callback) {
        if (sound == null) {
            callback.cancel();
        }
    }
}
