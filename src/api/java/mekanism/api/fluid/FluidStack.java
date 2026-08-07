package mekanism.api.fluid;

import io.netty.handler.codec.DecoderException;
import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

public final class FluidStack {

    public static final FluidStack EMPTY = new FluidStack(Fluids.EMPTY, 0);

    private final Fluid fluid;
    private int amount;
    @Nullable
    private CompoundTag tag;

    public FluidStack(Fluid fluid, int milliBuckets) {
        this(fluid, milliBuckets, null);
    }

    public FluidStack(Fluid fluid, int milliBuckets, @Nullable CompoundTag tag) {
        this.fluid = Objects.requireNonNull(fluid);
        if (BuiltInRegistries.FLUID.getId(fluid) < 0) {
            throw new IllegalArgumentException("Fluid must be registered");
        }
        this.amount = milliBuckets;
        this.tag = tag == null ? null : tag.copy();
    }

    public FluidStack(FluidStack stack, int milliBuckets) {
        this(stack.getFluid(), milliBuckets, stack.tag);
    }

    public static FluidStack fromVariant(FluidVariant variant, int milliBuckets) {
        return variant.isBlank() || milliBuckets <= 0 ? EMPTY : new FluidStack(variant.getFluid(), milliBuckets, variant.getNbt());
    }

    public FluidVariant getVariant() {
        return isEmpty() ? FluidVariant.blank() : FluidVariant.of(fluid, tag);
    }

    public Fluid getFluid() {
        return isEmpty() ? Fluids.EMPTY : fluid;
    }

    public Fluid getRawFluid() {
        return fluid;
    }

    public boolean isEmpty() {
        return fluid == Fluids.EMPTY || amount <= 0;
    }

    public int getAmount() {
        return isEmpty() ? 0 : amount;
    }

    public void setAmount(int milliBuckets) {
        checkMutable();
        amount = milliBuckets;
    }

    public void grow(int milliBuckets) {
        setAmount(Math.addExact(amount, milliBuckets));
    }

    public void shrink(int milliBuckets) {
        setAmount(Math.subtractExact(amount, milliBuckets));
    }

    public boolean hasTag() {
        return tag != null;
    }

    @Nullable
    public CompoundTag getTag() {
        return tag;
    }

    public void setTag(@Nullable CompoundTag tag) {
        checkMutable();
        this.tag = tag == null ? null : tag.copy();
    }

    public FluidStack copy() {
        return isEmpty() ? EMPTY : new FluidStack(fluid, amount, tag);
    }

    public boolean isFluidEqual(FluidStack other) {
        return getFluid() == other.getFluid() && Objects.equals(tag, other.tag);
    }

    public boolean isFluidStackIdentical(FluidStack other) {
        return isFluidEqual(other) && getAmount() == other.getAmount();
    }

    public boolean containsFluid(FluidStack other) {
        return isFluidEqual(other) && getAmount() >= other.getAmount();
    }

    public static boolean areFluidStackTagsEqual(FluidStack first, FluidStack second) {
        return Objects.equals(first.tag, second.tag);
    }

    public CompoundTag writeToNBT(CompoundTag nbt) {
        nbt.putString("FluidName", BuiltInRegistries.FLUID.getKey(getFluid()).toString());
        nbt.putInt("Amount", getAmount());
        if (tag == null) {
            nbt.remove("Tag");
        } else {
            nbt.put("Tag", tag.copy());
        }
        return nbt;
    }

    public static FluidStack loadFluidStackFromNBT(@Nullable CompoundTag nbt) {
        if (nbt == null || !nbt.contains("FluidName", Tag.TAG_STRING)) {
            return EMPTY;
        }
        ResourceLocation id = ResourceLocation.tryParse(nbt.getString("FluidName"));
        if (id == null || !BuiltInRegistries.FLUID.containsKey(id)) {
            return EMPTY;
        }
        Fluid fluid = BuiltInRegistries.FLUID.get(id);
        int amount = nbt.getInt("Amount");
        if (fluid == Fluids.EMPTY || amount <= 0) {
            return EMPTY;
        }
        return new FluidStack(fluid, amount, nbt.contains("Tag", Tag.TAG_COMPOUND) ? nbt.getCompound("Tag") : null);
    }

    public void writeToPacket(FriendlyByteBuf buffer) {
        buffer.writeVarInt(BuiltInRegistries.FLUID.getId(getFluid()));
        buffer.writeVarInt(getAmount());
        buffer.writeNbt(tag);
    }

    public static FluidStack readFromPacket(FriendlyByteBuf buffer) {
        Fluid fluid = BuiltInRegistries.FLUID.getHolder(buffer.readVarInt())
              .orElseThrow(() -> new DecoderException("Unknown fluid registry id")).value();
        int amount = buffer.readVarInt();
        CompoundTag tag = buffer.readNbt();
        return fluid == Fluids.EMPTY || amount <= 0 ? EMPTY : new FluidStack(fluid, amount, tag);
    }

    public Component getDisplayName() {
        return FluidVariantAttributes.getName(getVariant());
    }

    public String getTranslationKey() {
        Component name = getDisplayName();
        return name.getContents() instanceof TranslatableContents contents ? contents.getKey()
              : Util.makeDescriptionId("fluid", BuiltInRegistries.FLUID.getKey(getFluid()));
    }

    private void checkMutable() {
        if (fluid == Fluids.EMPTY) {
            throw new IllegalStateException("Cannot modify an empty fluid type");
        }
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof FluidStack stack && isFluidEqual(stack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFluid(), tag);
    }
}
