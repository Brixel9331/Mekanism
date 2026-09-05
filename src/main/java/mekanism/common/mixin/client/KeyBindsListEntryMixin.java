package mekanism.common.mixin.client;

import com.mojang.blaze3d.platform.InputConstants;
import mekanism.client.key.MekKeyBinding;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.controls.KeyBindsList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(KeyBindsList.KeyEntry.class)
public abstract class KeyBindsListEntryMixin {

    @Redirect(method = "*", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;setKey(Lnet/minecraft/client/KeyMapping;Lcom/mojang/blaze3d/platform/InputConstants$Key;)V"))
    private void mekanism$resetBinding(Options options, KeyMapping mapping, InputConstants.Key key) {
        if (mapping instanceof MekKeyBinding mek) {
            mek.setToDefault();
            options.save();
        } else {
            options.setKey(mapping, key);
        }
    }
}
