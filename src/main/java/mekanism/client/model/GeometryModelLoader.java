package mekanism.client.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import mekanism.client.model.baked.ExtensionBakedModel;
import mekanism.client.model.baked.MaterialBakedModel;
import mekanism.client.render.lib.QuadTransformation;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

public final class GeometryModelLoader {

    private static final FileToIdConverter MODEL_IDS = new FileToIdConverter("models", ".json");

    private GeometryModelLoader() {
    }

    public static void register(Map<ResourceLocation, Function<JsonObject, ModelGeometry>> loaders) {
        Map<ResourceLocation, Function<JsonObject, ModelGeometry>> registered = Map.copyOf(loaders);
        PreparableModelLoadingPlugin.register((resources, executor) -> CompletableFuture.supplyAsync(() -> prepare(resources, registered), executor),
              GeometryModelLoader::initialize);
    }

    private static PreparedModels prepare(ResourceManager resources, Map<ResourceLocation, Function<JsonObject, ModelGeometry>> loaders) {
        Map<ResourceLocation, Definition> definitions = new HashMap<>();
        Map<ResourceLocation, ResourceLocation> parents = new HashMap<>();
        Map<ResourceLocation, JsonObject> inlineModels = new HashMap<>();
        for (Map.Entry<ResourceLocation, Resource> entry : MODEL_IDS.listMatchingResources(resources).entrySet()) {
            ResourceLocation id = MODEL_IDS.fileToId(entry.getKey());
            try (Reader reader = entry.getValue().openAsReader()) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                String parent = GsonHelper.getAsString(json, "parent", "");
                if (!parent.isEmpty()) {
                    parents.put(id, new ResourceLocation(parent));
                }
                inspect(id, json, loaders, definitions, parents, inlineModels, false);
            } catch (IOException | RuntimeException error) {
                LogUtils.getLogger().warn("Unable to inspect model {}", id, error);
            }
        }
        Set<ResourceLocation> additional = new HashSet<>(definitions.keySet());
        additional.addAll(inlineModels.keySet());
        definitions.values().forEach(definition -> {
            if (definition.geometry() != null) {
                additional.addAll(definition.geometry().getDependencies());
            }
        });
        for (ResourceLocation id : parents.keySet()) {
            Set<ResourceLocation> visited = new HashSet<>();
            for (ResourceLocation current = id; current != null && visited.add(current); current = parents.get(current)) {
                if (definitions.containsKey(current)) {
                    additional.add(id);
                    break;
                }
            }
        }
        return new PreparedModels(Map.copyOf(definitions), Set.copyOf(additional), Map.copyOf(inlineModels));
    }

    private static void inspect(ResourceLocation id, JsonObject json, Map<ResourceLocation, Function<JsonObject, ModelGeometry>> loaders,
          Map<ResourceLocation, Definition> definitions, Map<ResourceLocation, ResourceLocation> parents,
          Map<ResourceLocation, JsonObject> inlineModels, boolean inline) {
        try {
            if (json.has("transform")) {
                ModelRootTransform.read(json.get("transform"));
                inlineModels.put(id, json);
            }
            String parent = GsonHelper.getAsString(json, "parent", "");
            if (!parent.isEmpty()) {
                parents.put(id, new ResourceLocation(parent));
            }
            String loaderName = GsonHelper.getAsString(json, "loader", "");
            ResourceLocation loaderId = ResourceLocation.tryParse(loaderName);
            Function<JsonObject, ModelGeometry> loader = loaderId == null ? null : loaders.get(loaderId);
            ModelGeometry geometry = null;
            if (loader != null) {
                geometry = loader.apply(json);
            } else if (loaderName.equals("forge:composite")) {
                geometry = new CompositeModelGeometry(inlineModels(id, "children", GsonHelper.getAsJsonObject(json, "children"), loaders,
                      definitions, parents, inlineModels), json.getAsJsonArray("item_render_order"));
            } else if (loaderName.equals("forge:separate_transforms")) {
                JsonObject base = new JsonObject();
                base.add("base", GsonHelper.getAsJsonObject(json, "base"));
                ResourceLocation baseId = inlineModels(id, "base", base, loaders, definitions, parents, inlineModels).get("base");
                geometry = new SeparateTransformsModelGeometry(baseId, inlineModels(id, "perspectives", GsonHelper.getAsJsonObject(json, "perspectives"),
                      loaders, definitions, parents, inlineModels));
            } else if (loaderName.equals("forge:item_layers")) {
                geometry = new ItemLayerModelGeometry(json);
            } else if (loaderName.isEmpty() && ElementModelGeometry.hasFaceData(json)) {
                geometry = new ElementModelGeometry(json);
            }
            BlendMode blendMode = blendMode(json);
            if (inline || geometry != null || blendMode != BlendMode.DEFAULT || json.has("transform")) {
                definitions.put(id, new Definition(geometry, blendMode, null));
            }
        } catch (RuntimeException error) {
            definitions.put(id, new Definition(null, BlendMode.DEFAULT, error));
            LogUtils.getLogger().error("Unable to load model geometry {}", id, error);
        }
    }

    private static Map<String, ResourceLocation> inlineModels(ResourceLocation owner, String group, JsonObject children,
          Map<ResourceLocation, Function<JsonObject, ModelGeometry>> loaders, Map<ResourceLocation, Definition> definitions,
          Map<ResourceLocation, ResourceLocation> parents, Map<ResourceLocation, JsonObject> inlineModels) {
        Map<String, ResourceLocation> parts = new LinkedHashMap<>();
        int index = 0;
        for (Map.Entry<String, com.google.gson.JsonElement> entry : children.entrySet()) {
            ResourceLocation id = owner.withSuffix("/.mekanism/" + group + "/" + index++);
            JsonObject json = entry.getValue().getAsJsonObject();
            inlineModels.put(id, json);
            inspect(id, json, loaders, definitions, parents, inlineModels, true);
            parts.put(entry.getKey(), id);
        }
        return parts;
    }

    private static BlendMode blendMode(JsonObject json) {
        return switch (GsonHelper.getAsString(json, "render_type", "")) {
            case "minecraft:solid" -> BlendMode.SOLID;
            case "minecraft:cutout" -> BlendMode.CUTOUT;
            case "minecraft:cutout_mipped" -> BlendMode.CUTOUT_MIPPED;
            case "minecraft:translucent" -> BlendMode.TRANSLUCENT;
            default -> BlendMode.DEFAULT;
        };
    }

    private static void initialize(PreparedModels prepared, ModelLoadingPlugin.Context context) {
        Map<BlockModel, Definition> models = Collections.synchronizedMap(new IdentityHashMap<>());
        context.resolveModel().register(resolve -> {
            ResourceLocation id = resolve.id();
            if (id instanceof ModelResourceLocation variant && "inventory".equals(variant.getVariant())) {
                id = new ResourceLocation(id.getNamespace(), "item/" + id.getPath());
            }
            JsonObject inline = prepared.inlineModels().get(id);
            if (inline == null) {
                return null;
            }
            JsonObject definition = inline.deepCopy();
            var transform = definition.remove("transform");
            BlockModel model = BlockModel.fromString(definition.toString());
            if (transform != null) {
                model.setRootTransform(ModelRootTransform.read(transform));
            }
            return model;
        });
        context.addModels(prepared.additional());
        context.modifyModelOnLoad().register((model, load) -> {
            if (model instanceof BlockModel blockModel) {
                ResourceLocation id = load.id();
                if (id instanceof ModelResourceLocation variant && "inventory".equals(variant.getVariant())) {
                    id = new ResourceLocation(id.getNamespace(), "item/" + id.getPath());
                }
                Definition definition = prepared.definitions().get(id);
                if (definition != null) {
                    models.put(blockModel, definition);
                }
            }
            return model;
        });
        context.modifyModelBeforeBake().register((model, bake) -> {
            if (model instanceof BlockModel blockModel) {
                BlendMode blendMode = BlendMode.DEFAULT;
                boolean defined = false;
                for (BlockModel current = blockModel; current != null; current = current.parent) {
                    Definition definition = models.get(current);
                    if (definition != null) {
                        defined = true;
                        if (blendMode == BlendMode.DEFAULT) {
                            blendMode = definition.blendMode();
                        }
                        if (definition.failure() != null) {
                            return bake.baker().getModel(ModelBakery.MISSING_MODEL_LOCATION);
                        }
                        if (definition.geometry() != null && (definition.geometry().getClass() != ElementModelGeometry.class ||
                            blockModel.getElements() == current.getElements())) {
                            return new GeometryModel(blockModel, definition.geometry(), blendMode);
                        }
                    }
                }
                if (defined) {
                    return new GeometryModel(blockModel, null, blendMode);
                }
            }
            return model;
        });
    }

    private record PreparedModels(Map<ResourceLocation, Definition> definitions, Set<ResourceLocation> additional,
                                  Map<ResourceLocation, JsonObject> inlineModels) {
    }

    private record Definition(@Nullable ModelGeometry geometry, BlendMode blendMode, @Nullable RuntimeException failure) {
    }

    private record GeometryModel(BlockModel source, @Nullable ModelGeometry geometry, BlendMode blendMode) implements UnbakedModel {

        @Override
        public Collection<ResourceLocation> getDependencies() {
            return source.getDependencies();
        }

        @Override
        public void resolveParents(Function<ResourceLocation, UnbakedModel> resolver) {
            source.resolveParents(resolver);
        }

        @Override
        public BakedModel bake(ModelBaker baker, Function<Material, TextureAtlasSprite> sprites, ModelState state, ResourceLocation id) {
            BakedModel original = source.bake(baker, sprites, state, id);
            if (geometry == null && source.getCustomGeometry() == null) {
                QuadTransformation rootTransform = ModelGeometry.rootTransform(source, state);
                if (rootTransform != QuadTransformation.identity) {
                    original = new ExtensionBakedModel(original) {
                        @Override
                        protected QuadTransformation getTransformation() {
                            return rootTransform;
                        }
                    };
                }
            }
            return geometry == null ? new MaterialBakedModel(original, blendMode) : geometry.bake(source, original, baker, sprites, state, id, blendMode);
        }
    }
}
