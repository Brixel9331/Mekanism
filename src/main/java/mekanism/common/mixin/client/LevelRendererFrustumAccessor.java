package mekanism.common.mixin.client;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LevelRenderer.class)
public interface LevelRendererFrustumAccessor {

    @Nullable
    @Accessor("cullingFrustum")
    Frustum mekanism$getCullingFrustum();

    @Nullable
    @Accessor("capturedFrustum")
    Frustum mekanism$getCapturedFrustum();
}
