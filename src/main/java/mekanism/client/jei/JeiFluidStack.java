package mekanism.client.jei;

import java.util.List;
import java.util.Optional;
import mekanism.api.fluid.FluidAmounts;
import mekanism.api.fluid.FluidStack;
import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

public final class JeiFluidStack implements IJeiFluidIngredient {

    private final FluidVariant variant;
    private final long amount;

    private JeiFluidStack(FluidStack stack) {
        variant = stack.getVariant();
        amount = FluidAmounts.toFabric(stack.getAmount());
    }

    @Nullable
    public static IJeiFluidIngredient from(FluidStack stack) {
        return stack.isEmpty() ? null : new JeiFluidStack(stack);
    }

    @Nullable
    static Object forIngredientLookup(Object ingredient) {
        return ingredient instanceof FluidStack stack ? from(stack) : ingredient;
    }

    static Object forGhostIngredient(Object ingredient) {
        if (ingredient instanceof IJeiFluidIngredient fluid) {
            if (fluid.getFluid() == Fluids.EMPTY || fluid.getAmount() <= 0) {
                return FluidStack.EMPTY;
            }
            int amount = Math.max(1, FluidAmounts.toMilliBuckets(fluid.getAmount()));
            return new FluidStack(fluid.getFluid(), amount, fluid.getTag().orElse(null));
        }
        return ingredient;
    }

    public static long getRendererCapacity(List<FluidStack> stacks) {
        int capacity = stacks.stream().mapToInt(FluidStack::getAmount).filter(amount -> amount > 0).max()
              .orElse(FluidAmounts.MILLI_BUCKETS_PER_BUCKET);
        return FluidAmounts.toFabric(capacity);
    }

    @Override
    public Fluid getFluid() {
        return variant.getFluid();
    }

    @Override
    public long getAmount() {
        return amount;
    }

    @Override
    public Optional<CompoundTag> getTag() {
        return Optional.ofNullable(variant.copyNbt());
    }
}
