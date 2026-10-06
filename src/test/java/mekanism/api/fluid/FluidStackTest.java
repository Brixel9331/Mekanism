package mekanism.api.fluid;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class FluidStackTest {

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void readsOriginalMilliBucketSaveFormat() {
        CompoundTag saved = new CompoundTag();
        saved.putString("FluidName", "minecraft:water");
        saved.putInt("Amount", 1_000);
        saved.put("Tag", tagged(7));

        FluidStack loaded = FluidStack.loadFluidStackFromNBT(saved);

        assertSame(Fluids.WATER, loaded.getFluid());
        assertEquals(1_000, loaded.getAmount());
        assertEquals(saved, loaded.writeToNBT(new CompoundTag()));
        saved.getCompound("Tag").putInt("quality", 8);
        assertEquals(7, loaded.getTag().getInt("quality"));
    }

    @Test
    void copiesTagsAtStorageAndVariantBoundaries() {
        CompoundTag tag = tagged(7);
        FluidStack stack = new FluidStack(Fluids.WATER, 1_000, tag);
        FluidStack copy = stack.copy();
        FluidVariant variant = stack.getVariant();
        CompoundTag saved = stack.writeToNBT(new CompoundTag());

        tag.putInt("quality", 8);
        copy.getTag().putInt("quality", 9);
        saved.getCompound("Tag").putInt("quality", 10);
        assertEquals(7, stack.getTag().getInt("quality"));

        stack.getTag().putInt("quality", 11);
        assertEquals(7, variant.getNbt().getInt("quality"));
        FluidStack restored = FluidStack.fromVariant(variant, 500);
        restored.getTag().putInt("quality", 12);
        assertEquals(7, variant.getNbt().getInt("quality"));
        assertEquals(500, restored.getAmount());
    }

    @ParameterizedTest
    @ValueSource(strings = {"missing:fluid", "bad id", "minecraft:empty"})
    void invalidSavedFluidsBecomeEmpty(String name) {
        CompoundTag saved = new CompoundTag();
        saved.putString("FluidName", name);
        saved.putInt("Amount", 1_000);
        assertSame(FluidStack.EMPTY, FluidStack.loadFluidStackFromNBT(saved));
    }

    @Test
    void emptyAndNonpositiveSavesBecomeEmpty() {
        assertSame(FluidStack.EMPTY, FluidStack.loadFluidStackFromNBT(null));
        assertSame(FluidStack.EMPTY, FluidStack.loadFluidStackFromNBT(new CompoundTag()));
        CompoundTag saved = new CompoundTag();
        saved.putString("FluidName", "minecraft:water");
        saved.putInt("Amount", -1);
        assertSame(FluidStack.EMPTY, FluidStack.loadFluidStackFromNBT(saved));
        saved.putInt("Amount", 0);
        assertSame(FluidStack.EMPTY, FluidStack.loadFluidStackFromNBT(saved));
    }

    @Test
    void nativePacketsRoundTripWithoutConsumingFollowingFields() {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            FluidStack water = new FluidStack(Fluids.WATER, Integer.MAX_VALUE, tagged(7));
            FluidStack lava = new FluidStack(Fluids.LAVA, 1);
            water.writeToPacket(buffer);
            FluidStack.EMPTY.writeToPacket(buffer);
            lava.writeToPacket(buffer);
            buffer.writeInt(1234);

            assertTrue(water.isFluidStackIdentical(FluidStack.readFromPacket(buffer)));
            assertSame(FluidStack.EMPTY, FluidStack.readFromPacket(buffer));
            assertTrue(lava.isFluidStackIdentical(FluidStack.readFromPacket(buffer)));
            assertEquals(1234, buffer.readInt());
            assertFalse(buffer.isReadable());
        } finally {
            buffer.release();
        }
    }

    @Test
    void rejectsUnknownPacketIds() {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            buffer.writeVarInt(Integer.MAX_VALUE);
            assertThrows(DecoderException.class, () -> FluidStack.readFromPacket(buffer));
        } finally {
            buffer.release();
        }
    }

    @Test
    void equalityIncludesTagsAndIgnoresAmounts() {
        FluidStack small = new FluidStack(Fluids.WATER, 1, tagged(7));
        FluidStack large = new FluidStack(Fluids.WATER, 1_000, tagged(7));
        FluidStack differentTag = new FluidStack(Fluids.WATER, 1, tagged(8));

        assertEquals(small, large);
        assertEquals(small.hashCode(), large.hashCode());
        assertFalse(small.isFluidStackIdentical(large));
        assertTrue(large.containsFluid(small));
        assertFalse(small.containsFluid(large));
        assertNotEquals(small, differentTag);
        assertNotEquals(small, new FluidStack(Fluids.LAVA, 1, tagged(7)));
    }

    @Test
    void preservesRawFluidWhenAnOwnedStackIsEmptied() {
        FluidStack stack = new FluidStack(Fluids.WATER, 1);
        stack.shrink(1);
        assertTrue(stack.isEmpty());
        assertSame(Fluids.EMPTY, stack.getFluid());
        assertSame(Fluids.WATER, stack.getRawFluid());
        assertTrue(stack.getVariant().isBlank());
        stack.grow(2);
        assertEquals(2, stack.getAmount());
        assertSame(Fluids.WATER, stack.getFluid());
        assertThrows(IllegalStateException.class, () -> FluidStack.EMPTY.setAmount(1));
        assertThrows(IllegalStateException.class, () -> FluidStack.EMPTY.setTag(tagged(7)));
    }

    @Test
    void overwritingSavedFluidClearsOldTags() {
        CompoundTag target = new FluidStack(Fluids.WATER, 1, tagged(7)).writeToNBT(new CompoundTag());
        new FluidStack(Fluids.LAVA, 1_000).writeToNBT(target);
        assertFalse(target.contains("Tag"));
        assertSame(Fluids.LAVA, FluidStack.loadFluidStackFromNBT(target).getFluid());
    }

    @Test
    void convertsBucketAndIntegerBoundsWithoutOverflow() {
        assertEquals(FluidConstants.BUCKET, FluidAmounts.toFabric(1_000));
        assertEquals(1_000, FluidAmounts.toMilliBuckets(FluidConstants.BUCKET));
        assertEquals(173_946_175_407L, FluidAmounts.toFabric(Integer.MAX_VALUE));
        assertEquals(Integer.MAX_VALUE, FluidAmounts.toMilliBuckets(Long.MAX_VALUE));
        assertThrows(IllegalArgumentException.class, () -> FluidAmounts.toFabric(-1));
        assertThrows(IllegalArgumentException.class, () -> FluidAmounts.toMilliBuckets(-1));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, 1, 80, 81, 82, 162, 80_999, 81_000, 81_001, 173_946_175_407L})
    void conversionLeavesFractionalMilliBucketsWithTheCaller(long requested) {
        long transferred = FluidAmounts.toFabric(FluidAmounts.toMilliBuckets(requested));
        assertTrue(transferred <= requested);
        assertTrue(requested - transferred < FluidAmounts.FABRIC_UNITS_PER_MILLI_BUCKET);
    }

    @Test
    void flowingVariantNormalizesAtTheFabricBoundary() {
        FluidStack stack = new FluidStack(Fluids.FLOWING_WATER, 1_000);
        assertSame(Fluids.WATER, stack.getVariant().getFluid());
        assertEquals("block.minecraft.water", new FluidStack(Fluids.WATER, 1).getTranslationKey());
    }

    private static CompoundTag tagged(int quality) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("quality", quality);
        return tag;
    }
}
