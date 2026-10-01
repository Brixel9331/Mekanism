package mekanism.common.mixin.client;

import mekanism.client.render.RenderPropertiesProvider;
import mekanism.common.block.BlockBounding;
import mekanism.common.block.BlockMekanism;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {

    @Shadow
    protected ClientLevel level;

    @Inject(method = "destroy", at = @At("HEAD"), cancellable = true)
    private void mekanism$destroyParticles(BlockPos pos, BlockState state, CallbackInfo callback) {
        if (state.getBlock() instanceof BlockMekanism && RenderPropertiesProvider.particles()
              .addDestroyEffects(state, level, pos, (ParticleEngine) (Object) this)) {
            callback.cancel();
        }
    }

    @Inject(method = "crack", at = @At("HEAD"), cancellable = true)
    private void mekanism$boundingParticles(BlockPos pos, Direction side, CallbackInfo callback) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof BlockBounding && RenderPropertiesProvider.boundingParticles()
              .addHitEffects(state, level, new BlockHitResult(Vec3.atCenterOf(pos), side, pos, false), (ParticleEngine) (Object) this)) {
            callback.cancel();
        }
    }
}
