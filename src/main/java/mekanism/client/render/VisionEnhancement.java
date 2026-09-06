package mekanism.client.render;

import mekanism.client.render.FogRenderCallback.FogDistance;
import net.minecraft.world.level.material.FogType;
import org.joml.Vector3f;

public final class VisionEnhancement {

    private VisionEnhancement() {
    }

    public static void adjustColor(Vector3f color) {
        color.mul(0.1F).add(0.9F * 0.4F, 0.9F * 0.8F, 0.9F * 0.4F);
    }

    public static FogDistance adjustDistance(FogType type, FogDistance distance, int installed, int maximum) {
        float far = distance.far();
        if (far < 20) {
            float scalar;
            if (type == FogType.LAVA) {
                //Special handling for lava which is usually either at 1 or 3
                scalar = 24 * far;
            } else {
                //Shortly before 27 this ends up being 192, but we want to get it beforehand, so we just allow numbers below 20
                scalar = 5 + 2.5F * (float) Math.pow(Math.E, 0.16F * far);
            }
            //192 is roughly equivalent to what spectators have lava, powdered snow, and a couple other bounds for fog are,
            // so we want to make sure we don't go above that
            far = Math.min(192, scalar);
        }
        //Scale the distance based on the number of installed modules
        far *= (float) Math.pow(installed, 1.25) / maximum;
        //This near plane is the same as spectators have set for lava and powdered snow
        return new FogDistance(-8, far);
    }
}
