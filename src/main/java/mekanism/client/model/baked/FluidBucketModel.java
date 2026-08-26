package mekanism.client.model.baked;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import mekanism.common.registration.impl.FluidRegistryObject;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class FluidBucketModel extends ForwardingBakedModel {

    private static final Map<ResourceLocation, FluidRegistryObject<?, ?, ?, ?, ?>> BUCKETS = new HashMap<>();
    private static boolean registered;
    private final List<BakedQuad> fluidQuads;
    @Nullable
    private final Mesh fluidMesh;

    public static void register(FluidRegistryObject<?, ?, ?, ?, ?> entry) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(entry.getBucket());
        BUCKETS.put(id.withPrefix("item/"), entry);
        BUCKETS.put(new ModelResourceLocation(id, "inventory"), entry);
        if (!registered) {
            ModelLoadingPlugin.register(context -> context.modifyModelAfterBake().register((model, bake) -> {
                FluidRegistryObject<?, ?, ?, ?, ?> bucket = BUCKETS.get(bake.id());
                if (bucket != null && model != null && !(model instanceof FluidBucketModel) && bake.baker().getModel(bake.id()) instanceof BlockModel source &&
                    source.hasTexture("fluid") && source.hasTexture("fluid_mask")) {
                    return new FluidBucketModel(model, bake.textureGetter().apply(source.getMaterial("fluid_mask")),
                          bake.textureGetter().apply(source.getMaterial("fluid")), bake.settings(), bake.id(), bucket.getFluidType().getLightLevel() > 0);
                }
                return model;
            }));
            registered = true;
        }
    }

    public FluidBucketModel(BakedModel base, TextureAtlasSprite mask, TextureAtlasSprite fluid, ModelState state, ResourceLocation id, boolean emissive) {
        wrapped = base;
        fluidQuads = createFluidQuads(mask.contents(), fluid, state, id);
        Renderer renderer = RendererAccess.INSTANCE.getRenderer();
        if (renderer == null) {
            fluidMesh = null;
        } else {
            MeshBuilder builder = renderer.meshBuilder();
            QuadEmitter emitter = builder.getEmitter();
            RenderMaterial material = renderer.materialFinder().emissive(emissive).find();
            for (BakedQuad quad : fluidQuads) {
                emitter.fromVanilla(quad, material, null).emit();
            }
            fluidMesh = builder.build();
        }
    }

    @Override
    public boolean isVanillaAdapter() {
        return fluidMesh == null;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, RandomSource random) {
        List<BakedQuad> base = wrapped.getQuads(state, face, random);
        if (face != null) {
            return base;
        }
        List<BakedQuad> quads = new ArrayList<>(base.size() + fluidQuads.size());
        quads.addAll(base);
        quads.addAll(fluidQuads);
        return quads;
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<RandomSource> random, RenderContext context) {
        wrapped.emitItemQuads(stack, random, context);
        if (fluidMesh != null) {
            fluidMesh.outputTo(context.getEmitter());
        }
    }

    private static List<BakedQuad> createFluidQuads(SpriteContents mask, TextureAtlasSprite fluid, ModelState state, ResourceLocation id) {
        int width = mask.width();
        int height = mask.height();
        boolean[][] pixels = new boolean[height][width];
        mask.getUniqueFrames().forEach(frame -> {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    pixels[y][x] |= !mask.isTransparent(frame, x, y);
                }
            }
        });
        List<BakedQuad> quads = new ArrayList<>();
        FaceBakery bakery = new FaceBakery();
        float dx = 16F / width;
        float dy = 16F / height;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!pixels[y][x]) {
                    continue;
                }
                int end = x + 1;
                while (end < width && pixels[y][end]) {
                    end++;
                }
                addQuad(quads, bakery, fluid, state, id, x * dx, y * dy, end * dx, (y + 1) * dy, Direction.NORTH);
                addQuad(quads, bakery, fluid, state, id, x * dx, y * dy, end * dx, (y + 1) * dy, Direction.SOUTH);
                addQuad(quads, bakery, fluid, state, id, x * dx, y * dy, (x + 1) * dx, (y + 1) * dy, Direction.WEST);
                addQuad(quads, bakery, fluid, state, id, (end - 1) * dx, y * dy, end * dx, (y + 1) * dy, Direction.EAST);
                for (int column = x; column < end; column++) {
                    if (y == 0 || !pixels[y - 1][column]) {
                        addQuad(quads, bakery, fluid, state, id, column * dx, y * dy, (column + 1) * dx, (y + 1) * dy, Direction.UP);
                    }
                    if (y == height - 1 || !pixels[y + 1][column]) {
                        addQuad(quads, bakery, fluid, state, id, column * dx, y * dy, (column + 1) * dx, (y + 1) * dy, Direction.DOWN);
                    }
                }
                x = end - 1;
            }
        }
        return List.copyOf(quads);
    }

    private static void addQuad(List<BakedQuad> quads, FaceBakery bakery, TextureAtlasSprite fluid, ModelState state, ResourceLocation id,
          float left, float top, float right, float bottom, Direction face) {
        float[] uv = face == Direction.NORTH ? new float[]{right, top, left, bottom} : new float[]{left, top, right, bottom};
        quads.add(bakery.bakeQuad(new Vector3f(left, 16 - bottom, 7.49F), new Vector3f(right, 16 - top, 8.51F),
              new BlockElementFace(null, 1, "fluid", new BlockFaceUV(uv, 0)), fluid, face, state, null, false, id));
    }
}
