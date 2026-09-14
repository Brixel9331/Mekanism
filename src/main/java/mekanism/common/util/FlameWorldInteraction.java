package mekanism.common.util;

import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

public final class FlameWorldInteraction {

    private FlameWorldInteraction() {
    }

    public static boolean ignite(Level world, @Nullable Entity shooter, BlockPos pos, Direction face) {
        if (world.isClientSide) {
            return false;
        }
        BlockState state = world.getBlockState(pos);
        if (!state.getFluidState().isEmpty()) {
            return false;
        }
        if (CampfireBlock.canLight(state)) {
            return BlockInteraction.tryPlace(world, shooter, pos, face, state.setValue(BlockStateProperties.LIT, true));
        }
        BlockPos adjacent = pos.relative(face);
        if (BaseFireBlock.canBePlacedAt(world, adjacent, face)) {
            return BlockInteraction.tryPlace(world, shooter, adjacent, face, BaseFireBlock.getState(world, adjacent));
        }
        if (state.getBlock() instanceof TntBlock && isFlammable(state)) {
            if (shooter instanceof Player player && (!world.mayInteract(player, pos) || !BlockInteraction.canBreak(world, player, pos, state))) {
                return false;
            }
            if (world.removeBlock(pos, false)) {
                TntBlock.explode(world, pos, shooter instanceof LivingEntity livingEntity ? livingEntity : null);
                return true;
            }
        }
        return false;
    }

    private static boolean isFlammable(BlockState state) {
        FlammableBlockRegistry.Entry entry = FlammableBlockRegistry.getDefaultInstance().get(state.getBlock());
        return state.ignitedByLava() || entry != null && entry.getBurnChance() > 0;
    }
}
