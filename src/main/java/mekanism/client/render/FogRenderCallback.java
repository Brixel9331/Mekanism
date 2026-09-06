package mekanism.client.render;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer.FogMode;
import org.joml.Vector3f;

public final class FogRenderCallback {

    public static final Event<Color> COLOR = EventFactory.createArrayBacked(Color.class, listeners -> (camera, partialTick, color) -> {
        for (Color listener : listeners) {
            listener.render(camera, partialTick, color);
        }
    });
    public static final Event<Distance> DISTANCE = EventFactory.createArrayBacked(Distance.class, listeners -> (camera, mode, partialTick, distance) -> {
        for (Distance listener : listeners) {
            distance = listener.render(camera, mode, partialTick, distance);
        }
        return distance;
    });

    private FogRenderCallback() {
    }

    @FunctionalInterface
    public interface Color {

        void render(Camera camera, float partialTick, Vector3f color);
    }

    @FunctionalInterface
    public interface Distance {

        FogDistance render(Camera camera, FogMode mode, float partialTick, FogDistance distance);
    }

    public record FogDistance(float near, float far) {
    }
}
