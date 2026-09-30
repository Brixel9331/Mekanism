package mekanism.client.model.robit;

import com.google.gson.JsonObject;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import mekanism.client.model.ElementModelGeometry;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;

public class RobitModel extends ElementModelGeometry {

    private final Function<ResourceLocation, TextureAtlasSprite> textures;
    private final UnaryOperator<ItemOverrides> overrides;

    public RobitModel(JsonObject json, Function<ResourceLocation, TextureAtlasSprite> textures, UnaryOperator<ItemOverrides> overrides) {
        super(json);
        this.textures = textures;
        this.overrides = overrides;
    }

    @Override
    protected BakedModel wrap(BakedModel original, BlendMode blendMode) {
        return new RobitBakedModel(original, textures, overrides);
    }
}
