package mekanism.common.mixin;

import mekanism.common.lib.BlockBreakSpeedCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockBehaviour.class)
public abstract class BlockBreakSpeedMixin {

    @Redirect(method = "getDestroyProgress", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getDestroySpeed(Lnet/minecraft/world/level/block/state/BlockState;)F"))
    private float mekanism$breakSpeed(Player player, BlockState state, BlockState targetState, Player breakingPlayer, BlockGetter level, BlockPos pos) {
        return BlockBreakSpeedCallback.EVENT.invoker().modifySpeed(player, state, pos, player.getDestroySpeed(state));
    }
}
