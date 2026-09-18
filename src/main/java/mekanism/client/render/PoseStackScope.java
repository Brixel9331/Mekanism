package mekanism.client.render;

import com.mojang.blaze3d.vertex.PoseStack;

public final class PoseStackScope implements AutoCloseable {

    private final PoseStack stack;
    private final PoseStack.Pose previous;

    public PoseStackScope(PoseStack stack) {
        this.stack = stack;
        previous = stack.last();
        stack.pushPose();
    }

    @Override
    public void close() {
        while (stack.last() != previous) {
            if (stack.clear()) {
                throw new IllegalStateException("Renderer removed its parent pose");
            }
            stack.popPose();
        }
    }
}
