package mekanism.common.capabilities.fluid;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.NBTConstants;
import mekanism.api.fluid.FluidStack;
import mekanism.common.capabilities.DynamicHandler.InteractPredicate;
import net.minecraft.SharedConstants;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BasicFluidTankTest {

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void simulationDoesNotChangeContentsOrNotifyListeners() {
        AtomicInteger changes = new AtomicInteger();
        BasicFluidTank tank = BasicFluidTank.create(1_000, changes::incrementAndGet);
        FluidStack supplied = water(1_500);

        assertEquals(500, tank.insert(supplied, Action.SIMULATE, AutomationType.EXTERNAL).getAmount());
        assertTrue(tank.isEmpty());
        assertEquals(0, changes.get());
        assertEquals(500, tank.insert(supplied, Action.EXECUTE, AutomationType.EXTERNAL).getAmount());
        assertEquals(1_500, supplied.getAmount());
        assertEquals(1_000, tank.getFluidAmount());
        assertEquals(1, changes.get());

        assertEquals(700, tank.extract(700, Action.SIMULATE, AutomationType.EXTERNAL).getAmount());
        assertEquals(1_000, tank.getFluidAmount());
        assertEquals(1, changes.get());
        assertEquals(1_000, tank.extract(2_000, Action.EXECUTE, AutomationType.EXTERNAL).getAmount());
        assertTrue(tank.isEmpty());
        assertEquals(2, changes.get());
    }

    @Test
    void rejectsDifferentFluidsAndTags() {
        BasicFluidTank tank = BasicFluidTank.create(1_000, null);
        FluidStack tagged = water(500);
        CompoundTag tag = new CompoundTag();
        tag.putInt("quality", 7);
        tagged.setTag(tag);
        tank.insert(tagged, Action.EXECUTE, AutomationType.EXTERNAL);

        assertEquals(100, tank.insert(water(100), Action.EXECUTE, AutomationType.EXTERNAL).getAmount());
        assertEquals(100, tank.insert(new FluidStack(Fluids.LAVA, 100), Action.EXECUTE, AutomationType.EXTERNAL).getAmount());
        assertEquals(500, tank.getFluidAmount());
        tagged.getTag().putInt("quality", 8);
        assertEquals(7, tank.getFluid().getTag().getInt("quality"));
    }

    @Test
    void inputAndOutputTanksRespectAutomationPermissions() {
        BasicFluidTank input = BasicFluidTank.input(1_000, stack -> stack.getFluid() == Fluids.WATER, null);
        assertEquals(100, input.insert(new FluidStack(Fluids.LAVA, 100), Action.EXECUTE, AutomationType.EXTERNAL).getAmount());
        input.insert(water(100), Action.EXECUTE, AutomationType.EXTERNAL);
        assertTrue(input.extract(100, Action.EXECUTE, AutomationType.EXTERNAL).isEmpty());
        assertEquals(100, input.extract(100, Action.EXECUTE, AutomationType.INTERNAL).getAmount());

        BasicFluidTank output = BasicFluidTank.output(1_000, null);
        assertEquals(100, output.insert(water(100), Action.EXECUTE, AutomationType.EXTERNAL).getAmount());
        assertTrue(output.insert(water(100), Action.EXECUTE, AutomationType.INTERNAL).isEmpty());
        assertEquals(100, output.extract(100, Action.EXECUTE, AutomationType.EXTERNAL).getAmount());
    }

    @Test
    void rateLimitsApplyInSimulationAndExecution() {
        BasicFluidTank tank = new BasicFluidTank(1_000, BasicFluidTank.alwaysTrueBi, BasicFluidTank.alwaysTrueBi, BasicFluidTank.alwaysTrue, null) {
            @Override
            protected int getRate(AutomationType automationType) {
                return 125;
            }
        };
        assertEquals(875, tank.insert(water(1_000), Action.SIMULATE, AutomationType.EXTERNAL).getAmount());
        assertEquals(875, tank.insert(water(1_000), Action.EXECUTE, AutomationType.EXTERNAL).getAmount());
        tank.setStack(water(1_000));
        assertEquals(125, tank.extract(500, Action.SIMULATE, AutomationType.EXTERNAL).getAmount());
        assertEquals(125, tank.extract(500, Action.EXECUTE, AutomationType.EXTERNAL).getAmount());
        assertEquals(875, tank.getFluidAmount());
    }

    @Test
    void capacityBoundsDoNotOverflow() {
        BasicFluidTank tank = BasicFluidTank.create(Integer.MAX_VALUE, null);
        tank.setStack(water(Integer.MAX_VALUE - 1));
        assertEquals(Integer.MAX_VALUE - 1, tank.insert(water(Integer.MAX_VALUE), Action.EXECUTE, AutomationType.EXTERNAL).getAmount());
        assertEquals(Integer.MAX_VALUE, tank.getFluidAmount());
        assertEquals(0, tank.growStack(Integer.MAX_VALUE, Action.EXECUTE));
        assertEquals(Integer.MAX_VALUE, tank.shrinkStack(Integer.MAX_VALUE, Action.EXECUTE));
        assertTrue(tank.isEmpty());
        assertThrows(IllegalArgumentException.class, () -> BasicFluidTank.create(-1, null));
    }

    @Test
    void tankNbtKeepsTheOriginalStoredEnvelope() {
        BasicFluidTank tank = BasicFluidTank.create(1_000, null);
        CompoundTag fluid = new CompoundTag();
        fluid.putString("FluidName", "minecraft:water");
        fluid.putInt("Amount", 750);
        CompoundTag saved = new CompoundTag();
        saved.put(NBTConstants.STORED, fluid);

        tank.deserializeNBT(saved);
        assertEquals(750, tank.getFluidAmount());
        assertEquals(saved, tank.serializeNBT());
        tank.deserializeNBT(new CompoundTag());
        assertEquals(750, tank.getFluidAmount());
        fluid.putString("FluidName", "missing:removed_fluid");
        tank.deserializeNBT(saved);
        assertTrue(tank.isEmpty());
    }

    @Test
    void extractionReturnsAnIndependentStack() {
        BasicFluidTank tank = BasicFluidTank.create(1_000, null);
        tank.setStack(water(1_000));
        FluidStack extracted = tank.extract(500, Action.EXECUTE, AutomationType.INTERNAL);
        extracted.setAmount(1);
        assertEquals(500, tank.getFluidAmount());
    }

    @Test
    void matchingTanksFillBeforeEmptyTanks() {
        BasicFluidTank empty = BasicFluidTank.create(1_000, null);
        BasicFluidTank matching = BasicFluidTank.create(1_000, null);
        matching.setStack(water(750));
        DynamicFluidHandler handler = new DynamicFluidHandler(side -> List.of(empty, matching), InteractPredicate.ALWAYS_TRUE,
              InteractPredicate.ALWAYS_TRUE, null);

        assertTrue(handler.insertFluid(water(500), Direction.NORTH, Action.SIMULATE).isEmpty());
        assertTrue(empty.isEmpty());
        assertEquals(750, matching.getFluidAmount());
        assertTrue(handler.insertFluid(water(500), Direction.NORTH, Action.EXECUTE).isEmpty());
        assertEquals(1_000, matching.getFluidAmount());
        assertEquals(250, empty.getFluidAmount());
    }

    @Test
    void extractionDoesNotMixFluidTypesAcrossTanks() {
        BasicFluidTank water = BasicFluidTank.create(1_000, null);
        BasicFluidTank lava = BasicFluidTank.create(1_000, null);
        water.setStack(water(250));
        lava.setStack(new FluidStack(Fluids.LAVA, 500));
        DynamicFluidHandler handler = new DynamicFluidHandler(side -> List.of(water, lava), InteractPredicate.ALWAYS_TRUE,
              InteractPredicate.ALWAYS_TRUE, null);

        FluidStack extracted = handler.extractFluid(1_000, Direction.NORTH, Action.EXECUTE);
        assertSame(Fluids.WATER, extracted.getFluid());
        assertEquals(250, extracted.getAmount());
        assertEquals(500, lava.getFluidAmount());
    }

    @Test
    void sidedAccessAndInvalidTankIndicesAreRespected() {
        BasicFluidTank tank = BasicFluidTank.create(1_000, null);
        DynamicFluidHandler handler = new DynamicFluidHandler(side -> side == Direction.DOWN ? List.of() : List.of(tank),
              (index, side) -> side == Direction.SOUTH, (index, side) -> side == Direction.NORTH, null);

        assertEquals(100, handler.insertFluid(water(100), Direction.SOUTH, Action.EXECUTE).getAmount());
        assertTrue(handler.insertFluid(water(100), Direction.NORTH, Action.EXECUTE).isEmpty());
        assertTrue(handler.extractFluid(100, Direction.NORTH, Action.EXECUTE).isEmpty());
        assertEquals(100, handler.extractFluid(100, Direction.SOUTH, Action.EXECUTE).getAmount());
        assertEquals(0, handler.getTanks(Direction.DOWN));
        assertEquals(100, handler.insertFluid(water(100), Direction.DOWN, Action.EXECUTE).getAmount());
        assertTrue(handler.getFluidInTank(-1, Direction.NORTH).isEmpty());
        assertTrue(handler.getFluidInTank(1, Direction.NORTH).isEmpty());
    }

    private static FluidStack water(int amount) {
        return new FluidStack(Fluids.WATER, amount);
    }
}
