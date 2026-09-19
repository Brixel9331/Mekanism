package mekanism.client.render.lib;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import org.joml.Vector3f;

public final class ModelQuadRenderer {

    private ModelQuadRenderer() {
    }

    public static void render(PoseStack.Pose pose, VertexConsumer consumer, BakedQuad quad, float red, float green, float blue, float alpha,
          int light, int overlay) {
        int[] vertices = quad.getVertices();
        Vector3f faceNormal = quad.getDirection().step();
        for (int offset = 0; offset < vertices.length; offset += 8) {
            int packedLight = vertices[offset + 6];
            int packedNormal = vertices[offset + 7];
            int combinedLight = Math.max(light & 65535, packedLight & 65535) | Math.max(light >>> 16, packedLight >>> 16) << 16;
            float normalX = packedNormal == 0 ? faceNormal.x() : (byte) packedNormal / 127F;
            float normalY = packedNormal == 0 ? faceNormal.y() : (byte) (packedNormal >> 8) / 127F;
            float normalZ = packedNormal == 0 ? faceNormal.z() : (byte) (packedNormal >> 16) / 127F;
            consumer.vertex(pose.pose(), Float.intBitsToFloat(vertices[offset]), Float.intBitsToFloat(vertices[offset + 1]), Float.intBitsToFloat(vertices[offset + 2]))
                  .color(red, green, blue, alpha)
                  .uv(Float.intBitsToFloat(vertices[offset + 4]), Float.intBitsToFloat(vertices[offset + 5]))
                  .overlayCoords(overlay).uv2(combinedLight).normal(pose.normal(), normalX, normalY, normalZ).endVertex();
        }
    }
}
