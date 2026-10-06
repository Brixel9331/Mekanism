package mekanism.common.mixin;

import mekanism.common.block.interfaces.IPortalFrameBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.portal.PortalShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PortalShape.class)
public abstract class PortalFrameMixin {

    @Shadow
    @Final
    @Mutable
    private static BlockBehaviour.StatePredicate FRAME;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void mekanism$portalFrames(CallbackInfo callback) {
        BlockBehaviour.StatePredicate original = FRAME;
        FRAME = (state, world, pos) -> original.test(state, world, pos) ||
              state.getBlock() instanceof IPortalFrameBlock block && block.isPortalFrame(state, world, pos);
    }
}
