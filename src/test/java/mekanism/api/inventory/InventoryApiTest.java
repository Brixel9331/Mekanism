package mekanism.api.inventory;

import java.util.List;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import net.minecraft.SharedConstants;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InventoryApiTest {

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void insertionCopiesInputsAndSimulationLeavesContentsUnchanged() {
        TestSlot slot = new TestSlot(64);
        ItemStack input = new ItemStack(Items.DIAMOND, 80);
        assertEquals(16, slot.insertItem(input, Action.SIMULATE, AutomationType.INTERNAL).getCount());
        assertTrue(slot.isEmpty());
        assertEquals(0, slot.changes);
        assertEquals(16, slot.insertItem(input, Action.EXECUTE, AutomationType.INTERNAL).getCount());
        assertEquals(64, slot.getCount());
        assertEquals(80, input.getCount());
        input.setCount(1);
        assertEquals(64, slot.getCount());
        assertEquals(1, slot.changes);
    }

    @Test
    void differentlyTaggedStacksCannotMerge() {
        TestSlot slot = new TestSlot(64);
        ItemStack first = new ItemStack(Items.DIAMOND, 4);
        first.getOrCreateTag().putString("grade", "pure");
        slot.setStack(first);
        ItemStack different = first.copy();
        different.getOrCreateTag().putString("grade", "impure");
        assertEquals(4, slot.insertItem(different, Action.EXECUTE, AutomationType.EXTERNAL).getCount());
        assertTrue(slot.insertItem(first, Action.EXECUTE, AutomationType.EXTERNAL).isEmpty());
        assertEquals(8, slot.getCount());
    }

    @Test
    void extractingOversizedStoredStacksRespectsItemStackLimits() {
        TestSlot slot = new TestSlot(1_000);
        slot.setStack(new ItemStack(Items.DIAMOND, 500));
        assertEquals(64, slot.extractItem(500, Action.SIMULATE, AutomationType.INTERNAL).getCount());
        assertEquals(500, slot.getCount());
        assertEquals(64, slot.extractItem(500, Action.EXECUTE, AutomationType.INTERNAL).getCount());
        assertEquals(436, slot.getCount());
        assertTrue(slot.extractItem(0, Action.EXECUTE, AutomationType.INTERNAL).isEmpty());
    }

    @Test
    void growingNearIntegerCapacityCannotOverflow() {
        TestSlot slot = new TestSlot(Integer.MAX_VALUE);
        slot.setStack(new ItemStack(Items.DIAMOND, Integer.MAX_VALUE - 5));
        assertEquals(5, slot.growStack(Integer.MAX_VALUE, Action.SIMULATE));
        assertEquals(Integer.MAX_VALUE - 5, slot.getCount());
        assertEquals(5, slot.growStack(Integer.MAX_VALUE, Action.EXECUTE));
        assertEquals(Integer.MAX_VALUE, slot.getCount());
        assertEquals(0, slot.growStack(100, Action.EXECUTE));
    }

    @Test
    void sidedHandlersExposeOnlyTheRequestedSlotsAndKeepAutomationTypes() {
        TestSlot slot = new TestSlot(64);
        IMekanismInventory inventory = new TestInventory(slot);
        assertEquals(1, inventory.getSlots(Direction.NORTH));
        assertEquals(0, inventory.getSlots(Direction.SOUTH));
        assertEquals(1, inventory.getSlots());
        assertTrue(inventory.insertItem(0, new ItemStack(Items.DIAMOND, 2), Direction.NORTH, Action.EXECUTE).isEmpty());
        assertEquals(AutomationType.EXTERNAL, slot.lastAutomation);
        assertTrue(inventory.insertItem(0, new ItemStack(Items.DIAMOND, 2), false).isEmpty());
        assertEquals(AutomationType.INTERNAL, slot.lastAutomation);
        assertEquals(4, slot.getCount());
        assertEquals(4, inventory.extractItem(0, 4, true).getCount());
        assertEquals(4, slot.getCount());
        assertEquals(2, inventory.insertItem(0, new ItemStack(Items.DIAMOND, 2), Direction.SOUTH, Action.EXECUTE).getCount());
        assertEquals(4, slot.getCount());
    }

    @Test
    void invalidSlotsHaveSafeEmptyResults() {
        IMekanismInventory inventory = new TestInventory(new TestSlot(64));
        for (int index : new int[]{-1, 1, Integer.MAX_VALUE}) {
            assertTrue(inventory.getStackInSlot(index).isEmpty());
            assertTrue(inventory.extractItem(index, 10, false).isEmpty());
            assertEquals(2, inventory.insertItem(index, new ItemStack(Items.DIAMOND, 2), false).getCount());
            assertEquals(0, inventory.getSlotLimit(index));
            assertFalse(inventory.isItemValid(index, new ItemStack(Items.DIAMOND)));
            inventory.setStackInSlot(index, new ItemStack(Items.DIAMOND));
        }
        assertTrue(inventory.isInventoryEmpty(null));
    }

    private record TestInventory(TestSlot slot) implements IMekanismInventory {

        @Override
        public List<IInventorySlot> getInventorySlots(Direction side) {
            return side == null || side == Direction.NORTH ? List.of(slot) : List.of();
        }

        @Override
        public void onContentsChanged() {
            slot.onContentsChanged();
        }
    }

    private static final class TestSlot implements IInventorySlot {

        private ItemStack stack = ItemStack.EMPTY;
        private final int limit;
        private int changes;
        private AutomationType lastAutomation;

        private TestSlot(int limit) {
            this.limit = limit;
        }

        @Override
        public ItemStack getStack() {
            return stack;
        }

        @Override
        public void setStack(ItemStack stack) {
            this.stack = stack.copy();
            onContentsChanged();
        }

        @Override
        public ItemStack insertItem(ItemStack stack, Action action, AutomationType automationType) {
            lastAutomation = automationType;
            return IInventorySlot.super.insertItem(stack, action, automationType);
        }

        @Override
        public int getLimit(ItemStack stack) {
            return limit;
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return stack.is(Items.DIAMOND);
        }

        @Override
        public Slot createContainerSlot() {
            return null;
        }

        @Override
        public CompoundTag serializeNBT() {
            return stack.save(new CompoundTag());
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            setStack(ItemStack.of(nbt));
        }

        @Override
        public void onContentsChanged() {
            changes++;
        }
    }
}
