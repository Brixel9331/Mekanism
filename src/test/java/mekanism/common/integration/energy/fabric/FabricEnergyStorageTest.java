package mekanism.common.integration.energy.fabric;

import java.util.concurrent.atomic.AtomicInteger;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyConversion;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FabricEnergyStorageTest {

    @Test
    void insertionOnlyConsumesWholeExternalUnits() {
        BasicEnergyContainer container = BasicEnergyContainer.create(joules("4"), null);
        FabricEnergyStorage storage = storage(container, "2.5");
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(1, storage.insert(10, transaction));
            assertEquals(joules("2.5"), container.getEnergy());
            assertEquals(0, storage.insert(1, transaction));
            transaction.commit();
        }
        assertEquals(joules("2.5"), container.getEnergy());
        assertEquals(1, storage.getAmount());
        assertEquals(1, storage.getCapacity());
    }

    @Test
    void extractionPreservesFractionalRemainder() {
        BasicEnergyContainer container = BasicEnergyContainer.create(joules("100"), null);
        container.setEnergy(joules("3.75"));
        FabricEnergyStorage storage = storage(container, "2.5");
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(1, storage.extract(10, transaction));
            assertEquals(0, storage.extract(1, transaction));
            transaction.commit();
        }
        assertEquals(joules("1.25"), container.getEnergy());
    }

    @Test
    void abortedOuterTransactionRestoresContentsWithoutNotification() {
        AtomicInteger changes = new AtomicInteger();
        BasicEnergyContainer container = BasicEnergyContainer.create(joules("100"), changes::incrementAndGet);
        FabricEnergyStorage storage = storage(container, "2.5");
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(5, storage.insert(5, transaction));
            assertEquals(2, storage.extract(2, transaction));
            assertEquals(0, changes.get());
        }
        assertEquals(FloatingLong.ZERO, container.getEnergy());
        assertEquals(0, changes.get());
    }

    @Test
    void committedNestedTransactionStillRollsBackWithParent() {
        BasicEnergyContainer container = BasicEnergyContainer.create(joules("100"), null);
        FabricEnergyStorage storage = storage(container, "2.5");
        try (Transaction outer = Transaction.openOuter()) {
            assertEquals(2, storage.insert(2, outer));
            try (Transaction nested = outer.openNested()) {
                assertEquals(3, storage.insert(3, nested));
                nested.commit();
            }
            assertEquals(joules("12.5"), container.getEnergy());
        }
        assertEquals(FloatingLong.ZERO, container.getEnergy());
    }

    @Test
    void abortedNestedTransactionRestoresOnlyItsChanges() {
        AtomicInteger changes = new AtomicInteger();
        BasicEnergyContainer container = BasicEnergyContainer.create(joules("100"), changes::incrementAndGet);
        FabricEnergyStorage storage = storage(container, "2.5");
        try (Transaction outer = Transaction.openOuter()) {
            assertEquals(2, storage.insert(2, outer));
            try (Transaction nested = outer.openNested()) {
                assertEquals(3, storage.insert(3, nested));
            }
            assertEquals(joules("5"), container.getEnergy());
            assertEquals(0, changes.get());
            outer.commit();
        }
        assertEquals(joules("5"), container.getEnergy());
        assertEquals(1, changes.get());
    }

    @Test
    void wrappersForDifferentSidesShareTheSameSnapshot() {
        BasicEnergyContainer container = BasicEnergyContainer.create(joules("100"), null);
        FabricEnergyStorage first = storage(container, "2.5");
        FabricEnergyStorage second = storage(container, "2.5");
        try (Transaction outer = Transaction.openOuter()) {
            assertEquals(5, first.insert(5, outer));
            assertEquals(2, second.extract(2, outer));
        }
        assertEquals(FloatingLong.ZERO, container.getEnergy());
    }

    @Test
    void finalCommitNotifiesOnce() {
        AtomicInteger changes = new AtomicInteger();
        BasicEnergyContainer container = BasicEnergyContainer.create(joules("100"), changes::incrementAndGet);
        FabricEnergyStorage storage = storage(container, "2.5");
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(5, storage.insert(5, transaction));
            assertEquals(2, storage.extract(2, transaction));
            assertEquals(0, changes.get());
            transaction.commit();
        }
        assertEquals(1, changes.get());
        assertEquals(joules("7.5"), container.getEnergy());
    }

    @Test
    void externalAccessRespectsContainerPermissionsAndRates() {
        BasicEnergyContainer input = BasicEnergyContainer.input(joules("100"), null);
        FabricEnergyStorage storage = storage(input, "2.5");
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(4, storage.insert(4, transaction));
            assertEquals(0, storage.extract(4, transaction));
            transaction.commit();
        }
        BasicEnergyContainer limited = new BasicEnergyContainer(joules("100"), BasicEnergyContainer.alwaysTrue,
              BasicEnergyContainer.alwaysTrue, null) {
            @Override
            protected FloatingLong getRate(AutomationType automationType) {
                return joules("3.75");
            }
        };
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(1, storage(limited, "2.5").insert(4, transaction));
            transaction.commit();
        }
        assertEquals(joules("2.5"), limited.getEnergy());
    }

    @Test
    void changedAcceptanceBetweenSimulationAndExecutionRollsBack() {
        AtomicInteger changes = new AtomicInteger();
        BasicEnergyContainer container = new BasicEnergyContainer(joules("100"), BasicEnergyContainer.alwaysTrue,
              BasicEnergyContainer.alwaysTrue, changes::incrementAndGet) {
            @Override
            public FloatingLong insert(FloatingLong amount, Action action, AutomationType automationType) {
                if (action.execute()) {
                    FloatingLong accepted = amount.min(FloatingLong.ONE);
                    return amount.subtract(accepted).add(super.insert(accepted, action, automationType));
                }
                return super.insert(amount, action, automationType);
            }
        };
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, storage(container, "2.5").insert(4, transaction));
            transaction.commit();
        }
        assertEquals(FloatingLong.ZERO, container.getEnergy());
        assertEquals(0, changes.get());
    }

    @Test
    void longRequestsAreClampedWithoutCreatingEnergy() {
        BasicEnergyContainer container = BasicEnergyContainer.create(FloatingLong.MAX_VALUE, null);
        FabricEnergyStorage storage = storage(container, "2.5");
        long transferred;
        try (Transaction transaction = Transaction.openOuter()) {
            transferred = storage.insert(Long.MAX_VALUE, transaction);
            assertEquals(7_378_697_629_483_820_646L, transferred);
            transaction.commit();
        }
        assertEquals(FloatingLong.create(transferred).multiply(joules("2.5")), container.getEnergy());
    }

    @Test
    void rejectsNegativeAmountsAndIgnoresZero() {
        BasicEnergyContainer container = BasicEnergyContainer.create(joules("100"), null);
        FabricEnergyStorage storage = storage(container, "2.5");
        try (Transaction transaction = Transaction.openOuter()) {
            assertThrows(IllegalArgumentException.class, () -> storage.insert(-1, transaction));
            assertThrows(IllegalArgumentException.class, () -> storage.extract(-1, transaction));
            assertEquals(0, storage.insert(0, transaction));
            assertEquals(0, storage.extract(0, transaction));
            transaction.commit();
        }
        assertEquals(FloatingLong.ZERO, container.getEnergy());
    }

    @Test
    void nbtRetainsTheOriginalDecimalFormat() {
        BasicEnergyContainer container = BasicEnergyContainer.create(joules("100"), null);
        container.setEnergy(joules("17.3125"));
        CompoundTag saved = container.serializeNBT();
        assertEquals("17.3125", saved.getString("stored"));
        BasicEnergyContainer restored = BasicEnergyContainer.create(joules("100"), null);
        restored.deserializeNBT(saved);
        assertEquals(container.getEnergy(), restored.getEnergy());
        saved.putString("stored", "invalid");
        restored.deserializeNBT(saved);
        assertEquals(FloatingLong.ZERO, restored.getEnergy());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.1", "0.4", "1", "2.5", "10"})
    void transfersConserveJoulesAcrossContainers(String ratio) {
        BasicEnergyContainer source = BasicEnergyContainer.create(joules("10000"), null);
        BasicEnergyContainer target = BasicEnergyContainer.create(joules("37.3125"), null);
        source.setEnergy(joules("500.125"));
        FabricEnergyStorage from = storage(source, ratio);
        FabricEnergyStorage to = storage(target, ratio);
        FloatingLong initial = source.getEnergy().copy();
        for (int request = 1; request <= 100; request++) {
            try (Transaction transaction = Transaction.openOuter()) {
                long inserted = to.insert(Math.min(request, from.getAmount()), transaction);
                assertEquals(inserted, from.extract(inserted, transaction));
                transaction.commit();
            }
            assertEquals(initial, source.getEnergy().add(target.getEnergy()));
        }
    }

    private static FloatingLong joules(String value) {
        return FloatingLong.parseFloatingLong(value);
    }

    private static FabricEnergyStorage storage(BasicEnergyContainer container, String ratio) {
        FloatingLong factor = joules(ratio).copyAsConst();
        return new FabricEnergyStorage(container, new IEnergyConversion() {
            @Override
            public boolean isEnabled() {
                return true;
            }

            @Override
            public FloatingLong convertFrom(FloatingLong energy) {
                return energy.multiply(factor);
            }

            @Override
            public FloatingLong convertInPlaceFrom(FloatingLong energy) {
                return energy.timesEqual(factor);
            }

            @Override
            public FloatingLong convertTo(FloatingLong energy) {
                return energy.divide(factor);
            }

            @Override
            public FloatingLong convertInPlaceTo(FloatingLong energy) {
                return energy.divideEquals(factor);
            }

            @Override
            public String getTranslationKey() {
                return "energy.test";
            }
        });
    }
}
