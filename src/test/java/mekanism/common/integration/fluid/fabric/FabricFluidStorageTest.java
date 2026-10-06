package mekanism.common.integration.fluid.fabric;

import java.util.concurrent.atomic.AtomicInteger;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.fluid.FluidAmounts;
import mekanism.api.fluid.FluidStack;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class FabricFluidStorageTest {

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void transfersOnlyWholeMilliBuckets() {
        BasicFluidTank tank = BasicFluidTank.create(1_000, null);
        FabricFluidStorage storage = new FabricFluidStorage(tank);
        FluidVariant water = FluidVariant.of(Fluids.WATER);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, storage.insert(water, 80, transaction));
            assertEquals(81, storage.insert(water, 161, transaction));
            assertEquals(1, tank.getFluidAmount());
            assertEquals(0, storage.extract(water, 80, transaction));
            assertEquals(81, storage.extract(water, 161, transaction));
            transaction.commit();
        }
        assertTrue(tank.isEmpty());
        assertEquals(81_000, storage.getCapacity());
    }

    @Test
    void abortRestoresFluidAndNbtWithoutNotifications() {
        AtomicInteger changes = new AtomicInteger();
        BasicFluidTank tank = BasicFluidTank.create(1_000, changes::incrementAndGet);
        CompoundTag nbt = new CompoundTag();
        nbt.putString("grade", "pure");
        tank.setStack(new FluidStack(Fluids.WATER, 200, nbt));
        CompoundTag original = tank.serializeNBT();
        changes.set(0);
        FabricFluidStorage storage = new FabricFluidStorage(tank);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(units(200), storage.extract(tank.getFluid().getVariant(), Long.MAX_VALUE, transaction));
            assertEquals(units(500), storage.insert(FluidVariant.of(Fluids.LAVA), units(500), transaction));
            assertEquals(0, changes.get());
        }
        assertEquals(original, tank.serializeNBT());
        assertEquals(0, changes.get());
        tank.getFluid().getTag().putString("grade", "changed");
        assertEquals("pure", original.getCompound("stored").getCompound("Tag").getString("grade"));
    }

    @Test
    void nestedAbortRestoresParentStateAndCommitNotifiesOnce() {
        AtomicInteger changes = new AtomicInteger();
        BasicFluidTank tank = BasicFluidTank.create(1_000, changes::incrementAndGet);
        FabricFluidStorage storage = new FabricFluidStorage(tank);
        FluidVariant water = FluidVariant.of(Fluids.WATER);
        try (Transaction outer = Transaction.openOuter()) {
            assertEquals(units(200), storage.insert(water, units(200), outer));
            try (Transaction nested = outer.openNested()) {
                assertEquals(units(100), storage.extract(water, units(100), nested));
            }
            assertEquals(200, tank.getFluidAmount());
            assertEquals(0, changes.get());
            outer.commit();
        }
        assertEquals(200, tank.getFluidAmount());
        assertEquals(1, changes.get());
    }

    @Test
    void nestedCommitStillRollsBackWithOuterTransaction() {
        AtomicInteger changes = new AtomicInteger();
        BasicFluidTank tank = BasicFluidTank.create(1_000, changes::incrementAndGet);
        FabricFluidStorage storage = new FabricFluidStorage(tank);
        try (Transaction outer = Transaction.openOuter()) {
            try (Transaction nested = outer.openNested()) {
                assertEquals(units(200), storage.insert(FluidVariant.of(Fluids.WATER), units(200), nested));
                nested.commit();
            }
            assertEquals(200, tank.getFluidAmount());
        }
        assertTrue(tank.isEmpty());
        assertEquals(0, changes.get());
    }

    @Test
    void wrappersShareSnapshotsAndLiveViews() {
        BasicFluidTank tank = BasicFluidTank.create(1_000, null);
        FabricFluidStorage first = new FabricFluidStorage(tank);
        FabricFluidStorage second = new FabricFluidStorage(tank);
        StorageView<FluidVariant> view = second.iterator().next();
        assertTrue(view.isResourceBlank());
        try (Transaction transaction = Transaction.openOuter()) {
            first.insert(FluidVariant.of(Fluids.WATER), units(400), transaction);
            assertEquals(units(400), view.getAmount());
            assertEquals(units(150), view.extract(view.getResource(), units(150), transaction));
            assertEquals(units(250), first.getAmount());
        }
        assertTrue(view.isResourceBlank());
        assertEquals(0, first.getAmount());
        assertSame(first, first.getSlot(0));
        assertThrows(IndexOutOfBoundsException.class, () -> first.getSlot(1));
    }

    @Test
    void externalTransfersRespectPermissionsAndFluidValidation() {
        FluidVariant water = FluidVariant.of(Fluids.WATER);
        FluidVariant lava = FluidVariant.of(Fluids.LAVA);
        BasicFluidTank input = BasicFluidTank.input(1_000, stack -> stack.getFluid() == Fluids.WATER, null);
        FabricFluidStorage in = new FabricFluidStorage(input);
        BasicFluidTank output = BasicFluidTank.output(1_000, null);
        output.setStack(new FluidStack(Fluids.WATER, 300));
        FabricFluidStorage out = new FabricFluidStorage(output);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, in.insert(lava, units(100), transaction));
            assertEquals(units(100), in.insert(water, units(100), transaction));
            assertEquals(0, in.extract(water, units(100), transaction));
            assertEquals(0, out.insert(water, units(100), transaction));
            assertEquals(units(100), out.extract(water, units(100), transaction));
            transaction.commit();
        }
        assertEquals(100, input.getFluidAmount());
        assertEquals(200, output.getFluidAmount());
    }

    @Test
    void tankRateLimitsRemainInMilliBuckets() {
        BasicFluidTank tank = new BasicFluidTank(1_000, BasicFluidTank.alwaysTrueBi, BasicFluidTank.alwaysTrueBi, BasicFluidTank.alwaysTrue, null) {
            @Override
            protected int getRate(AutomationType automationType) {
                return 37;
            }
        };
        FabricFluidStorage storage = new FabricFluidStorage(tank);
        FluidVariant water = FluidVariant.of(Fluids.WATER);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(units(37), storage.insert(water, Long.MAX_VALUE, transaction));
            assertEquals(units(37), storage.insert(water, Long.MAX_VALUE, transaction));
            assertEquals(units(37), storage.extract(water, Long.MAX_VALUE, transaction));
            transaction.commit();
        }
        assertEquals(37, tank.getFluidAmount());
    }

    @Test
    void extractionAndInsertionRequireMatchingFluidNbt() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("grade", "pure");
        BasicFluidTank tank = BasicFluidTank.create(1_000, null);
        tank.setStack(new FluidStack(Fluids.WATER, 300, nbt));
        FabricFluidStorage storage = new FabricFluidStorage(tank);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, storage.extract(FluidVariant.of(Fluids.WATER), units(100), transaction));
            assertEquals(0, storage.insert(FluidVariant.of(Fluids.WATER), units(100), transaction));
            assertEquals(units(100), storage.extract(FluidVariant.of(Fluids.WATER, nbt), units(100), transaction));
            transaction.commit();
        }
        assertEquals(200, tank.getFluidAmount());
        assertEquals(nbt, tank.getFluid().getTag());
    }

    @Test
    void rejectedAndZeroTransfersNeverNotify() {
        AtomicInteger changes = new AtomicInteger();
        BasicFluidTank tank = BasicFluidTank.create(1_000, stack -> false, changes::incrementAndGet);
        FabricFluidStorage storage = new FabricFluidStorage(tank);
        FluidVariant water = FluidVariant.of(Fluids.WATER);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, storage.insert(water, units(100), transaction));
            assertEquals(0, storage.insert(water, 0, transaction));
            assertEquals(0, storage.extract(water, 0, transaction));
            assertThrows(IllegalArgumentException.class, () -> storage.insert(water, -1, transaction));
            assertThrows(IllegalArgumentException.class, () -> storage.extract(water, -1, transaction));
            assertThrows(IllegalArgumentException.class, () -> storage.insert(FluidVariant.blank(), 1, transaction));
            assertThrows(IllegalArgumentException.class, () -> storage.extract(FluidVariant.blank(), 1, transaction));
            transaction.commit();
        }
        assertEquals(0, changes.get());
    }

    @Test
    void oversizedRequestsCannotOverflowTankAmounts() {
        BasicFluidTank tank = BasicFluidTank.create(Integer.MAX_VALUE, null);
        FabricFluidStorage storage = new FabricFluidStorage(tank);
        FluidVariant water = FluidVariant.of(Fluids.WATER);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(units(Integer.MAX_VALUE), storage.insert(water, Long.MAX_VALUE, transaction));
            assertEquals(0, storage.insert(water, Long.MAX_VALUE, transaction));
            assertEquals(units(Integer.MAX_VALUE), storage.getAmount());
            assertEquals(units(Integer.MAX_VALUE), storage.extract(water, Long.MAX_VALUE, transaction));
            transaction.commit();
        }
        assertTrue(tank.isEmpty());
    }

    @Test
    void overflowTransfersRollbackAllParticipatingTanks() {
        AtomicInteger lowerChanges = new AtomicInteger();
        AtomicInteger upperChanges = new AtomicInteger();
        BasicFluidTank upper = BasicFluidTank.create(1_000, upperChanges::incrementAndGet);
        BasicFluidTank lower = new BasicFluidTank(1_000, BasicFluidTank.alwaysTrueBi, BasicFluidTank.alwaysTrueBi, BasicFluidTank.alwaysTrue,
              lowerChanges::incrementAndGet) {
            @Override
            public FluidStack insert(FluidStack stack, Action action, AutomationType automationType) {
                FluidStack remainder = super.insert(stack, action, automationType);
                return remainder.isEmpty() ? remainder : insertInto(upper, remainder, action, AutomationType.EXTERNAL);
            }
        };
        FabricFluidStorage storage = new FabricFluidStorage(lower);
        FluidVariant water = FluidVariant.of(Fluids.WATER);
        assertEquals(units(1_500), StorageUtil.simulateInsert(storage, water, units(1_500), null));
        assertTrue(lower.isEmpty());
        assertTrue(upper.isEmpty());
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(units(1_500), storage.insert(water, units(1_500), transaction));
            assertEquals(1_000, lower.getFluidAmount());
            assertEquals(500, upper.getFluidAmount());
            assertEquals(0, lowerChanges.get());
            assertEquals(0, upperChanges.get());
            transaction.commit();
        }
        assertEquals(1, lowerChanges.get());
        assertEquals(1, upperChanges.get());
    }

    @Test
    void exceptionsDuringMutationRollbackTheOperation() {
        AtomicInteger changes = new AtomicInteger();
        BasicFluidTank tank = new BasicFluidTank(1_000, BasicFluidTank.alwaysTrueBi, BasicFluidTank.alwaysTrueBi, BasicFluidTank.alwaysTrue,
              changes::incrementAndGet) {
            @Override
            public FluidStack insert(FluidStack stack, Action action, AutomationType automationType) {
                super.insert(stack, action, automationType);
                throw new IllegalStateException("rejected after mutation");
            }
        };
        FabricFluidStorage storage = new FabricFluidStorage(tank);
        try (Transaction transaction = Transaction.openOuter()) {
            assertThrows(IllegalStateException.class, () -> storage.insert(FluidVariant.of(Fluids.WATER), units(100), transaction));
            assertTrue(tank.isEmpty());
            transaction.commit();
        }
        assertEquals(0, changes.get());
        tank.setStack(new FluidStack(Fluids.WATER, 100));
        assertEquals(1, changes.get());
    }

    @Test
    void creativeTankBehaviorDoesNotModifyOrNotify() {
        AtomicInteger changes = new AtomicInteger();
        BasicFluidTank tank = new BasicFluidTank(1_000, BasicFluidTank.alwaysTrueBi, BasicFluidTank.alwaysTrueBi, BasicFluidTank.alwaysTrue,
              changes::incrementAndGet) {
            @Override
            public FluidStack insert(FluidStack stack, Action action, AutomationType automationType) {
                return super.insert(stack, Action.SIMULATE, automationType);
            }

            @Override
            public FluidStack extract(int amount, Action action, AutomationType automationType) {
                return super.extract(amount, Action.SIMULATE, automationType);
            }
        };
        tank.setStack(new FluidStack(Fluids.WATER, 500));
        changes.set(0);
        FabricFluidStorage storage = new FabricFluidStorage(tank);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(units(100), storage.insert(FluidVariant.of(Fluids.WATER), units(100), transaction));
            assertEquals(units(100), storage.extract(FluidVariant.of(Fluids.WATER), units(100), transaction));
            transaction.commit();
        }
        assertEquals(500, tank.getFluidAmount());
        assertEquals(0, changes.get());
    }

    @ParameterizedTest
    @ValueSource(longs = {1, 80, 81, 82, 8_100, 81_001, Long.MAX_VALUE})
    void fabricTransferUtilityConservesFluid(long requested) {
        BasicFluidTank source = BasicFluidTank.create(1_000, null);
        source.setStack(new FluidStack(Fluids.WATER, 1_000));
        BasicFluidTank destination = BasicFluidTank.create(750, null);
        try (Transaction transaction = Transaction.openOuter()) {
            long moved = StorageUtil.move(new FabricFluidStorage(source), new FabricFluidStorage(destination), variant -> true, requested, transaction);
            assertTrue(moved <= requested);
            assertEquals(0, moved % FluidAmounts.FABRIC_UNITS_PER_MILLI_BUCKET);
            assertEquals(units(destination.getFluidAmount()), moved);
            assertEquals(1_000, source.getFluidAmount() + destination.getFluidAmount());
            transaction.commit();
        }
        assertEquals(1_000, source.getFluidAmount() + destination.getFluidAmount());
    }

    private static long units(int milliBuckets) {
        return FluidAmounts.toFabric(milliBuckets);
    }
}
