package mekanism.client.model.baked;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import mekanism.client.render.lib.ModelQuadMaterials;
import mekanism.client.render.lib.Quad;
import mekanism.client.render.lib.QuadTransformation;
import mekanism.common.lib.render.DriveArrayModelData;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

public class DriveArrayBakedModel extends DynamicBakedModel<DriveArrayModelData> {

    private static final float[][] DRIVE_PLACEMENTS = {
          {0, 6F / 16}, {-2F / 16, 6F / 16}, {-4F / 16, 6F / 16}, {-7F / 16, 6F / 16}, {-9F / 16, 6F / 16}, {-11F / 16, 6F / 16},
          {0, 0}, {-2F / 16, 0}, {-4F / 16, 0}, {-7F / 16, 0}, {-9F / 16, 0}, {-11F / 16, 0}
    };

    private final Map<Integer, BakedModel> drives;
    private final Function<ItemStack, DriveArrayModelData> itemData;
    private final ItemOverrides overrides;
    private final LoadingCache<DriveKey, List<BakedQuad>> cache = CacheBuilder.newBuilder().maximumSize(256).build(new CacheLoader<>() {
        @Override
        public List<BakedQuad> load(DriveKey key) {
            return createDriveQuads(key);
        }
    });

    public DriveArrayBakedModel(BakedModel original, Map<Integer, BakedModel> drives, Function<ItemStack, DriveArrayModelData> itemData) {
        super(original, DriveArrayModelData.class, BlendMode.DEFAULT);
        this.drives = Map.copyOf(drives);
        this.itemData = itemData;
        this.overrides = new ItemOverrides(null, null, List.of()) {
            @Override
            public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
                BakedModel resolved = original.getOverrides().resolve(model, stack, level, entity, seed);
                DriveArrayBakedModel target = resolved == null || resolved == model || resolved == original ? DriveArrayBakedModel.this :
                      new DriveArrayBakedModel(resolved, drives, itemData);
                return target.bind(itemData.apply(stack));
            }
        };
    }

    @Override
    public ItemOverrides getOverrides() {
        return overrides;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random, @Nullable DriveArrayModelData data) {
        List<BakedQuad> base = wrapped.getQuads(state, side, random);
        if (side != null || data == null || data.statuses() == 0) {
            return base;
        }
        List<BakedQuad> result = new ArrayList<>(base);
        result.addAll(driveQuads(state, random, data));
        return result;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter view, BlockState state, BlockPos pos, Supplier<RandomSource> random, RenderContext context) {
        wrapped.emitBlockQuads(view, state, pos, random, context);
        emitDrives(state, random.get(), context, getRenderData(view, pos));
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<RandomSource> random, RenderContext context, @Nullable DriveArrayModelData data) {
        wrapped.emitItemQuads(stack, random, context);
        emitDrives(null, random.get(), context, data == null ? itemData.apply(stack) : data);
    }

    private void emitDrives(@Nullable BlockState state, RandomSource random, RenderContext context, @Nullable DriveArrayModelData data) {
        if (data != null && data.statuses() != 0) {
            for (BakedQuad quad : driveQuads(state, random, data)) {
                ModelQuadMaterials.emit(quad, null, BlendMode.CUTOUT, context.getEmitter());
            }
        }
    }

    private List<BakedQuad> driveQuads(@Nullable BlockState state, RandomSource random, DriveArrayModelData data) {
        Direction facing = state != null && state.hasProperty(BlockStateProperties.HORIZONTAL_FACING) ?
              state.getValue(BlockStateProperties.HORIZONTAL_FACING) : Direction.NORTH;
        return cache.getUnchecked(new DriveKey(facing, data, random.nextLong()));
    }

    private List<BakedQuad> createDriveQuads(DriveKey key) {
        List<BakedQuad> result = new ArrayList<>();
        QuadTransformation rotation = QuadTransformation.rotate(key.facing());
        RandomSource random = RandomSource.create(key.seed());
        for (int slot = 0; slot < DriveArrayModelData.SLOTS; slot++) {
            BakedModel model = drives.get(key.data().status(slot));
            if (model != null) {
                float[] translation = DRIVE_PLACEMENTS[slot];
                QuadTransformation transform = QuadTransformation.list(QuadTransformation.translate(translation[0], translation[1], 0), rotation,
                      QuadTransformation.filtered_fullbright);
                for (int face = 0; face <= Direction.values().length; face++) {
                    Direction side = face == Direction.values().length ? null : Direction.values()[face];
                    random.setSeed(key.seed());
                    for (BakedQuad original : model.getQuads(null, side, random)) {
                        Quad quad = new Quad(original);
                        quad.transform(transform);
                        result.add(quad.bake());
                    }
                }
            }
        }
        return List.copyOf(result);
    }

    private record DriveKey(Direction facing, DriveArrayModelData data, long seed) {
    }
}
