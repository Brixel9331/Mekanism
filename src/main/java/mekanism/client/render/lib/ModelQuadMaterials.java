package mekanism.client.render.lib;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public final class ModelQuadMaterials {

    private static final Map<Key, RenderMaterial> MATERIALS = new ConcurrentHashMap<>();

    private ModelQuadMaterials() {
    }

    public static void emit(BakedQuad quad, @Nullable Direction cullFace, BlendMode blendMode, QuadEmitter emitter) {
        Renderer renderer = RendererAccess.INSTANCE.getRenderer();
        boolean ambientOcclusion = !(quad instanceof ModelQuad modelQuad) || modelQuad.hasAmbientOcclusion();
        Key key = new Key(renderer, blendMode, isFullbright(quad), quad.isShade(), ambientOcclusion);
        RenderMaterial material = MATERIALS.computeIfAbsent(key, k -> k.renderer().materialFinder().blendMode(k.blendMode())
              .emissive(k.emissive()).disableDiffuse(!k.shade()).ambientOcclusion(k.ambientOcclusion() ? TriState.DEFAULT : TriState.FALSE).find());
        emitter.fromVanilla(quad, material, cullFace).emit();
    }

    public static boolean isFullbright(BakedQuad quad) {
        int[] vertices = quad.getVertices();
        for (int offset = 6; offset < vertices.length; offset += 8) {
            if ((vertices[offset] & LightTexture.FULL_BRIGHT) != LightTexture.FULL_BRIGHT) {
                return false;
            }
        }
        return true;
    }

    private record Key(Renderer renderer, BlendMode blendMode, boolean emissive, boolean shade, boolean ambientOcclusion) {
    }
}
