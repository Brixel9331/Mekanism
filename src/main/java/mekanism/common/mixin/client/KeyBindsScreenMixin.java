package mekanism.common.mixin.client;

import com.mojang.blaze3d.platform.InputConstants;
import mekanism.client.key.KeyModifier;
import mekanism.client.key.MekKeyBinding;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.controls.KeyBindsList;
import net.minecraft.client.gui.screens.controls.KeyBindsScreen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyBindsScreen.class)
public abstract class KeyBindsScreenMixin extends OptionsSubScreen {

    @Shadow
    public KeyMapping selectedKey;
    @Shadow
    public long lastKeySelection;
    @Shadow
    private KeyBindsList keyBindsList;

    protected KeyBindsScreenMixin(Screen parent, Options options, Component title) {
        super(parent, options, title);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void mekanism$bindKey(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> callback) {
        if (selectedKey instanceof MekKeyBinding mek) {
            InputConstants.Key key = keyCode == GLFW.GLFW_KEY_ESCAPE ? InputConstants.UNKNOWN : InputConstants.getKey(keyCode, scanCode);
            mek.setKey(key);
            mek.setKeyModifier(KeyModifier.active());
            if (!KeyModifier.isKeyCodeModifier(key)) {
                selectedKey = null;
            }
            mekanism$refreshBindings();
            callback.setReturnValue(true);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void mekanism$bindMouse(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> callback) {
        if (selectedKey instanceof MekKeyBinding mek) {
            mek.setKey(InputConstants.Type.MOUSE.getOrCreate(button));
            mek.setKeyModifier(KeyModifier.active());
            selectedKey = null;
            mekanism$refreshBindings();
            callback.setReturnValue(true);
        }
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (selectedKey instanceof MekKeyBinding mek && mek.getKey().equals(InputConstants.getKey(keyCode, scanCode))) {
            selectedKey = null;
            mekanism$refreshBindings();
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Redirect(method = "*", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;setKey(Lcom/mojang/blaze3d/platform/InputConstants$Key;)V"))
    private void mekanism$resetBinding(KeyMapping mapping, InputConstants.Key key) {
        if (mapping instanceof MekKeyBinding mek) {
            mek.setToDefault();
        } else {
            mapping.setKey(key);
        }
    }

    @Unique
    private void mekanism$refreshBindings() {
        lastKeySelection = Util.getMillis();
        keyBindsList.resetMappingAndUpdateButtons();
        options.save();
    }
}
