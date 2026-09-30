package mekanism.client.render.obj;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import io.github.fabricators_of_create.porting_lib.models.geometry.IGeometryLoader;
import io.github.fabricators_of_create.porting_lib.models.obj.ObjLoader;
import io.github.fabricators_of_create.porting_lib.models.obj.ObjModel;
import java.util.Map;

public final class ObjModelLoader implements IGeometryLoader<ObjModel> {

    public static final ObjModelLoader INSTANCE = new ObjModelLoader();
    private static final Map<String, String> SETTINGS = Map.of(
          "automatic_culling", "automaticCulling", "shade_quads", "shadeQuads", "flip_v", "flipV",
          "emissive_ambient", "emissiveAmbient", "mtl_override", "mtlOverride");

    private ObjModelLoader() {
    }

    @Override
    public ObjModel read(JsonObject json, JsonDeserializationContext context) {
        JsonObject settings = json.deepCopy();
        SETTINGS.forEach((original, target) -> {
            if (settings.has(original)) {
                settings.add(target, settings.get(original));
            }
        });
        if (!settings.has("flipV")) {
            settings.addProperty("flipV", false);
        }
        return ObjLoader.INSTANCE.read(settings, context);
    }
}
