package mekanism.common.util;

import mekanism.api.fluid.FluidAmounts;
import mekanism.api.fluid.FluidStack;
import mekanism.common.registration.impl.FluidDeferredRegister.MekanismFluidType;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FluidWorldInteraction {

    private FluidWorldInteraction() {
    }

    public static boolean canPlace(FluidStack stack) {
        return !stack.isEmpty() && !stack.getFluid().defaultFluidState().createLegacyBlock().isAir();
    }

    public static FluidStack pickupSource(Level world, BlockPos pos, BlockState state) {
        FluidState fluidState = state.getFluidState();
        if (world.isClientSide() || fluidState.isEmpty() || !fluidState.isSource() || !(state.getBlock() instanceof BucketPickup pickup)) {
            return FluidStack.EMPTY;
        }
        ItemStack container = pickup.pickupBlock(world, pos, state);
        if (container.isEmpty()) {
            return FluidStack.EMPTY;
        }
        FluidState remaining = world.getBlockState(pos).getFluidState();
        if (remaining.isSource() && remaining.getType().isSame(fluidState.getType())) {
            return FluidStack.EMPTY;
        }
        Fluid fluid = container.getItem() instanceof BucketItem bucket ? bucket.content : fluidState.getType();
        return new FluidStack(fluid, FluidAmounts.MILLI_BUCKETS_PER_BUCKET);
    }

    public static boolean tryPlaceContainedLiquid(@Nullable Player player, Level world, BlockPos pos, @NotNull FluidStack fluidStack, @Nullable Direction side) {
        Fluid fluid = fluidStack.getFluid();
        if (!canPlace(fluidStack)) {
            //If there is no fluid, or it cannot be placed in the world just
            return false;
        }
        BlockState state = world.getBlockState(pos);
        boolean isReplaceable = state.canBeReplaced(fluid);
        boolean canContainFluid = state.getBlock() instanceof LiquidBlockContainer liquidBlockContainer && liquidBlockContainer.canPlaceLiquid(world, pos, state, fluid);
        if (state.isAir() || isReplaceable || canContainFluid) {
            if (world.dimensionType().ultraWarm() && vaporizes(world, pos, fluidStack)) {
                world.playSound(player, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F,
                      2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);
                for (int i = 0; i < 8; i++) {
                    world.addParticle(ParticleTypes.LARGE_SMOKE, pos.getX() + world.random.nextDouble(), pos.getY() + world.random.nextDouble(),
                          pos.getZ() + world.random.nextDouble(), 0, 0, 0);
                }
            } else if (canContainFluid) {
                if (!((LiquidBlockContainer) state.getBlock()).placeLiquid(world, pos, state, fluid.defaultFluidState())) {
                    //If something went wrong return that we couldn't actually place it
                    return false;
                }
                playEmptySound(player, world, pos, fluidStack);
            } else {
                if (!world.isClientSide() && isReplaceable && !state.liquid()) {
                    world.destroyBlock(pos, true);
                }
                if (!world.setBlock(pos, fluid.defaultFluidState().createLegacyBlock(), Block.UPDATE_ALL_IMMEDIATE) && !state.getFluidState().isSource()) {
                    return false;
                }
                playEmptySound(player, world, pos, fluidStack);
            }
            return true;
        }
        return side != null && tryPlaceContainedLiquid(player, world, pos.relative(side), fluidStack, null);
    }

    private static void playEmptySound(@Nullable Player player, LevelAccessor world, BlockPos pos, @NotNull FluidStack fluidStack) {
        SoundEvent soundevent = FluidVariantAttributes.getEmptySound(fluidStack.getVariant());
        if (soundevent == null) {
            soundevent = fluidStack.getFluid().is(FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
        }
        world.playSound(player, pos, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    private static boolean vaporizes(Level world, BlockPos pos, FluidStack stack) {
        if (FluidVariantAttributes.getHandler(stack.getFluid()) instanceof MekanismFluidType type) {
            return type.isVaporizedOnPlacement(world, pos, stack);
        }
        return stack.getFluid().is(FluidTags.WATER);
    }

    public static void playFillSound(@Nullable Player player, LevelAccessor world, BlockPos pos, @NotNull FluidStack fluidStack, @Nullable SoundEvent soundEvent) {
        if (soundEvent == null) {
            Fluid fluid = fluidStack.getFluid();
            soundEvent = fluid.getPickupSound().orElseGet(() -> FluidVariantAttributes.getFillSound(fluidStack.getVariant()));
        }
        if (soundEvent != null) {
            world.playSound(player, pos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

}
