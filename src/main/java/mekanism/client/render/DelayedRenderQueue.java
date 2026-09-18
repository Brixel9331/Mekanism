package mekanism.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import mekanism.common.base.ProfilerConstants;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class DelayedRenderQueue {

    private final Map<RenderType, List<RenderTask>> queued = new LinkedHashMap<>();

    public void add(RenderType type, RenderTask task) {
        queued.computeIfAbsent(type, key -> new ArrayList<>()).add(task);
    }

    public void clear() {
        queued.clear();
    }

    public boolean isEmpty() {
        return queued.isEmpty();
    }

    public void render(Camera camera, MultiBufferSource.BufferSource buffers, PoseStack pose, int renderTick, float partialTick, ProfilerFiller profiler) {
        if (queued.isEmpty()) {
            return;
        }
        Map<RenderType, List<RenderTask>> pending = new LinkedHashMap<>(queued);
        queued.clear();
        List<RenderBatch> batches = new ArrayList<>(pending.size());
        pending.forEach((type, tasks) -> {
            double closest = Double.MAX_VALUE;
            if (pending.size() > 1) {
                for (RenderTask task : tasks) {
                    Vec3 center = task.getCenterPos(partialTick);
                    if (center != null) {
                        closest = Math.min(closest, camera.getPosition().distanceToSqr(center));
                    }
                }
            }
            batches.add(new RenderBatch(type, tasks, closest));
        });
        batches.sort(Comparator.comparingDouble(RenderBatch::distanceSquared).reversed());
        profiler.push(ProfilerConstants.DELAYED);
        try (PoseStackScope stage = new PoseStackScope(pose)) {
            Vec3 position = camera.getPosition();
            pose.translate(-position.x, -position.y, -position.z);
            for (RenderBatch batch : batches) {
                VertexConsumer buffer = buffers.getBuffer(batch.type());
                try {
                    for (RenderTask task : batch.tasks()) {
                        String section = task.getProfilerSection();
                        if (section != null) {
                            profiler.push(section);
                        }
                        try (PoseStackScope taskPose = new PoseStackScope(pose)) {
                            task.render(camera, buffer, pose, renderTick, partialTick, profiler);
                        } finally {
                            if (section != null) {
                                profiler.pop();
                            }
                        }
                    }
                } finally {
                    buffers.endBatch(batch.type());
                }
            }
        } finally {
            profiler.pop();
        }
    }

    private record RenderBatch(RenderType type, List<RenderTask> tasks, double distanceSquared) {
    }

    @FunctionalInterface
    public interface RenderTask {

        void render(Camera camera, VertexConsumer buffer, PoseStack pose, int renderTick, float partialTick, ProfilerFiller profiler);

        @Nullable
        default Vec3 getCenterPos(float partialTick) {
            return null;
        }

        @Nullable
        default String getProfilerSection() {
            return null;
        }
    }
}
