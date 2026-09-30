package mekanism.client.model;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import mekanism.client.render.lib.Quad;
import mekanism.client.render.lib.QuadUtils;
import mekanism.client.render.lib.Vertex;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import io.github.fabricators_of_create.porting_lib.models.geometry.IUnbakedGeometry;
import io.github.fabricators_of_create.porting_lib.models.obj.ObjLoader;
import io.github.fabricators_of_create.porting_lib.models.obj.ObjModel;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

public class BaseModelCache {

    private static volatile ModelBaker currentBaker;

    public static ModelBaker getBaker() {
        return java.util.Objects.requireNonNull(currentBaker, "Models have not been baked");
    }

    private final Map<ResourceLocation, MekanismModelData> modelMap = new Object2ObjectOpenHashMap<>();

    private final String modid;

    protected BaseModelCache(String modid) {
        this.modid = modid;
    }

    private ResourceLocation rl(String path) {
        return new ResourceLocation(modid, path);
    }

    public void onBake(ModelManager evt) {
        modelMap.values().forEach(m -> m.reload(evt));
    }

    public void setup(ModelLoadingPlugin.Context event) {
        modelMap.values().forEach(mekanismModelData -> mekanismModelData.setup(event));
        event.modifyModelAfterBake().register((model, context) -> {
            currentBaker = context.baker();
            return model;
        });
    }

    protected OBJModelData registerOBJ(String path) {
        return registerOBJ(rl(path));
    }

    protected OBJModelData registerOBJ(ResourceLocation rl) {
        return register(rl, OBJModelData::new);
    }

    protected JSONModelData registerJSON(String path) {
        return registerJSON(rl(path));
    }

    protected JSONModelData registerJSON(ResourceLocation rl) {
        return register(rl, JSONModelData::new);
    }

    protected JSONModelData registerJSONAndBake(ResourceLocation rl) {
        ModelBaker baker = getBaker();
        //Register the model
        JSONModelData data = registerJSON(rl);
        //Manually run the JsonModelData#reload logic
        data.bakedModel = baker.bake(rl, BlockModelRotation.X0_Y0);
        if (baker.getModel(rl) instanceof BlockModel blockModel) {
            data.model = blockModel.getCustomGeometry();
        }
        return data;
    }

    protected <DATA extends MekanismModelData> DATA register(ResourceLocation rl, Function<ResourceLocation, DATA> creator) {
        DATA data = creator.apply(rl);
        modelMap.put(rl, data);
        return data;
    }

    public static BakedModel getBakedModel(ModelManager evt, ResourceLocation rl) {
        BakedModel bakedModel = evt.bakedRegistry.get(rl);
        if (bakedModel == null) {
            LogUtils.getLogger().error("Baked model doesn't exist: {}", rl);
            return evt.getMissingModel();
        }
        return bakedModel;
    }

    public static class MekanismModelData {

        protected IUnbakedGeometry<?> model;

        protected final ResourceLocation rl;
        private final Map<BlockModel, BakedModel> bakedMap = new Object2ObjectOpenHashMap<>();

        protected MekanismModelData(ResourceLocation rl) {
            this.rl = rl;
        }

        protected void reload(ModelManager evt) {
            bakedMap.clear();
        }

        protected void setup(ModelLoadingPlugin.Context event) {
        }

        public BakedModel bake(BlockModel config) {
            return bakedMap.computeIfAbsent(config, c -> {
                ModelBaker baker = getBaker();
                return model.bake(c, baker, Material::sprite, BlockModelRotation.X0_Y0, ItemOverrides.EMPTY, rl, false);
            });
        }

        public IUnbakedGeometry<?> getModel() {
            return model;
        }
    }

    public static class OBJModelData extends MekanismModelData {

        protected OBJModelData(ResourceLocation rl) {
            super(rl);
        }

        @Override
        protected void reload(ModelManager evt) {
            super.reload(evt);
            JsonObject definition = new JsonObject();
            definition.addProperty("model", rl.toString());
            definition.addProperty("shadeQuads", useDiffuseLighting());
            model = ObjLoader.INSTANCE.read(definition, null);
        }

        @Override
        public ObjModel getModel() {
            return (ObjModel) super.getModel();
        }

        protected boolean useDiffuseLighting() {
            return true;
        }
    }

    public static class JSONModelData extends MekanismModelData {

        private BakedModel bakedModel;

        private JSONModelData(ResourceLocation rl) {
            super(rl);
        }

        @Override
        protected void reload(ModelManager evt) {
            super.reload(evt);
            bakedModel = BaseModelCache.getBakedModel(evt, rl);
            UnbakedModel unbaked = getBaker().getModel(rl);
            if (unbaked instanceof BlockModel blockModel) {
                model = blockModel.getCustomGeometry();
            }
        }

        @Override
        protected void setup(ModelLoadingPlugin.Context event) {
            event.addModels(rl);
        }

        public void collectQuadVertices(List<Vertex[]> vertices, RandomSource random) {
            for (Quad quad : QuadUtils.unpack(getQuads(random))) {
                vertices.add(quad.getVertices());
            }
        }

        public List<BakedQuad> getQuads(RandomSource random) {
            //TODO: Decide if this should just redirect to the other get quads method (some impls might be different depending on if it gets data and render type vs not)
            return getBakedModel().getQuads(null, null, random);
        }

        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
            return getBakedModel().getQuads(state, side, rand);
        }

        public BakedModel getBakedModel() {
            return bakedModel;
        }
    }
}
