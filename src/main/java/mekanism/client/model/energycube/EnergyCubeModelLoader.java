package mekanism.client.model.energycube;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import mekanism.api.RelativeSide;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;

/**
 * Mekanism model loader that properly loads models in a specific to energy cube format
 */
public class EnergyCubeModelLoader {

    public static final EnergyCubeModelLoader INSTANCE = new EnergyCubeModelLoader();

    private EnergyCubeModelLoader() {
    }

    @NotNull
    public EnergyCubeGeometry read(@NotNull JsonObject jsonObject) {
        List<BlockElement> frame = readElements(jsonObject, "frame");
        Map<RelativeSide, List<BlockElement>> leds = new EnumMap<>(RelativeSide.class);
        Map<RelativeSide, List<BlockElement>> ports = new EnumMap<>(RelativeSide.class);
        for (RelativeSide side : RelativeSide.values()) {
            String name = side.name().toLowerCase(Locale.ROOT);
            leds.put(side, readElements(jsonObject, name + "LEDs"));
            ports.put(side, readElements(jsonObject, name + "Port"));
        }
        return new EnergyCubeGeometry(frame, leds, ports);
    }

    private List<BlockElement> readElements(JsonObject jsonObject, String key) {
        JsonObject model = new JsonObject();
        model.add("elements", GsonHelper.getAsJsonArray(jsonObject, key));
        List<BlockElement> elements = BlockModel.fromString(model.toString()).getElements();
        if (elements.isEmpty()) {
            throw new JsonParseException("Energy cube models requires a \"" + key + "\" element with at least one element.");
        }
        return elements;
    }
}
