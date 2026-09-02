package mekanism.common.integration.energy.fabric;

import mekanism.api.energy.IEnergyConversion;
import mekanism.api.energy.IStrictEnergyHandler;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

public final class FabricItemEnergyHandler {

    private FabricItemEnergyHandler() {
    }

    @Nullable
    public static IStrictEnergyHandler create(ItemStack stack, IEnergyConversion conversion) {
        if (stack.isEmpty()) {
            return null;
        }
        ContainerSlot slot = new ContainerSlot(stack);
        ContainerItemContext context = ContainerItemContext.ofSingleSlot(slot);
        return context.find(EnergyStorage.ITEM) == null ? null :
              new FabricStrictEnergyHandler(() -> {
                  slot.container = stack;
                  return context.find(EnergyStorage.ITEM);
              }, conversion, slot::isValid);
    }

    private static final class ContainerSlot extends SingleStackStorage {

        private final ItemStack original;
        private ItemStack container;

        private ContainerSlot(ItemStack original) {
            this.original = original;
            container = original;
        }

        @Override
        protected ItemStack getStack() {
            return container;
        }

        @Override
        protected void setStack(ItemStack stack) {
            container = stack;
        }

        private boolean isValid() {
            return ItemStack.isSameItem(original, container) && original.getCount() == container.getCount();
        }

        @Override
        protected void onFinalCommit() {
            if (isValid()) {
                original.setTag(container.hasTag() ? container.getTag().copy() : null);
                container = original;
            }
        }
    }
}
