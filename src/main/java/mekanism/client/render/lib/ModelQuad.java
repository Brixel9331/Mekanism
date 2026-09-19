package mekanism.client.render.lib;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

public final class ModelQuad extends BakedQuad {

    private final boolean ambientOcclusion;

    public ModelQuad(int[] vertices, int tintIndex, Direction direction, TextureAtlasSprite sprite, boolean shade, boolean ambientOcclusion) {
        super(vertices, tintIndex, direction, sprite, shade);
        this.ambientOcclusion = ambientOcclusion;
    }

    public boolean hasAmbientOcclusion() {
        return ambientOcclusion;
    }
}
