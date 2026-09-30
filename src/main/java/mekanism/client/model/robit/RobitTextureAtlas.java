package mekanism.client.model.robit;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

public final class RobitTextureAtlas {

    public static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS);

    private RobitTextureAtlas() {
    }

    public static ResourceLocation spriteId(ResourceLocation skin) {
        return skin.withPrefix("entity/robit/");
    }

    public static TextureAtlasSprite getSprite(ResourceLocation skin) {
        return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(spriteId(skin));
    }
}
