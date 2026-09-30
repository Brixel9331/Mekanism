package mekanism.client.model.robit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import mekanism.client.model.baked.DynamicBakedModel;
import mekanism.client.render.lib.QuadTransformation;
import mekanism.client.render.lib.QuadTransformation.TextureFilteredTransformation;
import mekanism.client.render.lib.QuadUtils;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class RobitBakedModel extends DynamicBakedModel<ResourceLocation> {

    private final Function<ResourceLocation, TextureAtlasSprite> textures;
    private final ItemOverrides overrides;
    private final LoadingCache<SkinKey, List<BakedQuad>> cache = CacheBuilder.newBuilder().maximumSize(256).build(new CacheLoader<>() {
        @Override
        public List<BakedQuad> load(SkinKey key) {
            QuadTransformation transformation = TextureFilteredTransformation.of(QuadTransformation.texture(key.texture()),
                  texture -> texture.equals(MissingTextureAtlasSprite.getLocation()));
            return List.copyOf(QuadUtils.transformBakedQuads(key.quads(), transformation));
        }
    });

    public RobitBakedModel(BakedModel original, Function<ResourceLocation, TextureAtlasSprite> textures, UnaryOperator<ItemOverrides> overrides) {
        super(original, ResourceLocation.class, BlendMode.CUTOUT);
        this.textures = textures;
        this.overrides = overrides.apply(original.getOverrides());
    }

    @Override
    public ItemOverrides getOverrides() {
        return overrides;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random, @Nullable ResourceLocation skin) {
        List<BakedQuad> quads = wrapped.getQuads(state, side, random);
        if (skin == null || quads.isEmpty()) {
            return quads;
        }
        TextureAtlasSprite texture = textures.apply(skin);
        return texture == null ? quads : cache.getUnchecked(new SkinKey(texture, List.copyOf(quads)));
    }

    private record SkinKey(TextureAtlasSprite texture, List<BakedQuad> quads) {
    }
}
