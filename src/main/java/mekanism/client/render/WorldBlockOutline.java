package mekanism.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Objects;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext.BlockOutlineContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

public final class WorldBlockOutline {

    private WorldBlockOutline() {
    }

    public static boolean render(WorldRenderContext context, BlockPos pos, BlockState state, Entity entity, BlockHitResult originalHit) {
        BlockPos offset = pos.subtract(originalHit.getBlockPos());
        BlockHitResult hit = new BlockHitResult(originalHit.getLocation().add(offset.getX(), offset.getY(), offset.getZ()), originalHit.getDirection(), pos,
              originalHit.isInside());
        if (!WorldRenderEvents.BEFORE_BLOCK_OUTLINE.invoker().beforeBlockOutline(context, hit)) {
            return false;
        }
        MultiBufferSource buffers = Objects.requireNonNull(context.consumers());
        Vec3 camera = context.camera().getPosition();
        BlockOutlineContext target = new OutlineTarget(buffers.getBuffer(RenderType.lines()), entity, camera.x, camera.y, camera.z, pos, state);
        boolean vanilla = WorldRenderEvents.BLOCK_OUTLINE.invoker().onBlockOutline(context, target);
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());
        if (vanilla) {
            renderShape(context.matrixStack(), lines, state.getShape(context.world(), pos, CollisionContext.of(entity)),
                  pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
        }
        return vanilla;
    }

    private static void renderShape(PoseStack pose, VertexConsumer lines, VoxelShape shape, double x, double y, double z) {
        PoseStack.Pose transform = pose.last();
        shape.forAllEdges((minX, minY, minZ, maxX, maxY, maxZ) -> {
            Vector3f normal = new Vector3f((float) (maxX - minX), (float) (maxY - minY), (float) (maxZ - minZ)).normalize();
            lines.vertex(transform.pose(), (float) (x + minX), (float) (y + minY), (float) (z + minZ))
                  .color(0, 0, 0, 0.4F).normal(transform.normal(), normal.x(), normal.y(), normal.z()).endVertex();
            lines.vertex(transform.pose(), (float) (x + maxX), (float) (y + maxY), (float) (z + maxZ))
                  .color(0, 0, 0, 0.4F).normal(transform.normal(), normal.x(), normal.y(), normal.z()).endVertex();
        });
    }

    private record OutlineTarget(VertexConsumer vertexConsumer, Entity entity, double cameraX, double cameraY, double cameraZ, BlockPos blockPos,
                                 BlockState blockState) implements BlockOutlineContext {
    }
}
