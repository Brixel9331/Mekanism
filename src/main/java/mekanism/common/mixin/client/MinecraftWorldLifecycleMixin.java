package mekanism.common.mixin.client;

import mekanism.common.lib.WorldLifecycle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftWorldLifecycleMixin {

    @Unique
    @Nullable
    private ClientLevel mekanism$previousWorld;

    @Inject(method = "updateLevelInEngines", at = @At("RETURN"))
    private void mekanism$updateWorld(@Nullable ClientLevel world, CallbackInfo ci) {
        if (mekanism$previousWorld != world) {
            if (mekanism$previousWorld != null) {
                WorldLifecycle.UNLOAD.invoker().onWorld(mekanism$previousWorld);
            }
            mekanism$previousWorld = world;
            if (world != null) {
                WorldLifecycle.LOAD.invoker().onWorld(world);
            }
        }
    }
}
