package mekanism.common.mixin.client;

import mekanism.client.ScreenChangeCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftScreenMixin {

    @Shadow
    @Nullable
    public Screen screen;

    @Inject(method = "setScreen", at = @At("HEAD"))
    private void mekanism$beforeScreenChange(@Nullable Screen next, CallbackInfo callback) {
        ScreenChangeCallback.EVENT.invoker().beforeChange((Minecraft) (Object) this, screen, next);
    }
}
