package mekanism.client.render;

import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.model.geom.ModelPart;

public final class ModelVisibility implements AutoCloseable {

    private Map<ModelPart, Boolean> original;

    public void hide(ModelPart part) {
        if (original == null) {
            original = new IdentityHashMap<>();
        }
        original.putIfAbsent(part, part.visible);
        part.visible = false;
    }

    public void apply() {
        if (original != null) {
            original.keySet().forEach(part -> part.visible = false);
        }
    }

    @Override
    public void close() {
        //Undo model visibility changes we made to ensure that other entities of the same type are properly visible
        if (original != null) {
            original.forEach((part, visible) -> part.visible = visible);
            original.clear();
        }
    }
}
