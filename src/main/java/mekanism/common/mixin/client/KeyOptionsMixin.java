package mekanism.common.mixin.client;

import com.mojang.blaze3d.platform.InputConstants;
import mekanism.client.key.KeyModifier;
import mekanism.client.key.MekKeyBinding;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Options.class)
public abstract class KeyOptionsMixin {

    @Unique
    private KeyModifier mekanism$loadedModifier = KeyModifier.NONE;

    @Redirect(method = "processOptions", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/InputConstants;getKey(Ljava/lang/String;)Lcom/mojang/blaze3d/platform/InputConstants$Key;"))
    private InputConstants.Key mekanism$loadKeyModifier(String saved) {
        MekKeyBinding.SavedBinding binding = MekKeyBinding.parseSavedBinding(saved);
        mekanism$loadedModifier = binding.modifier();
        return binding.key();
    }

    @Redirect(method = "processOptions", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;setKey(Lcom/mojang/blaze3d/platform/InputConstants$Key;)V"))
    private void mekanism$restoreKeyModifier(KeyMapping mapping, InputConstants.Key key) {
        mapping.setKey(key);
        if (mapping instanceof MekKeyBinding mek) {
            mek.setKeyModifier(mekanism$loadedModifier);
        }
        mekanism$loadedModifier = KeyModifier.NONE;
    }
}
