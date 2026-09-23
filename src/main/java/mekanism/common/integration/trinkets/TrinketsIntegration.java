package mekanism.common.integration.trinkets;

import dev.emi.trinkets.api.TrinketsApi;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class TrinketsIntegration {

    private TrinketsIntegration() {
    }

    public static Optional<Container> getInventory(LivingEntity entity) {
        return TrinketsApi.getTrinketComponent(entity).flatMap(component -> component.getInventory().values().stream()
              .flatMap(group -> group.values().stream()).map(inventory -> (Container) inventory).reduce(CompoundContainer::new));
    }

    public static List<ItemStack> getEquippedItems(LivingEntity entity) {
        return TrinketsApi.getTrinketComponent(entity).map(component -> component.getAllEquipped().stream()
              .map(pair -> pair.getB()).toList()).orElseGet(List::of);
    }

    public static Optional<SlotResult> findFirstResult(LivingEntity entity, Predicate<ItemStack> filter) {
        return TrinketsApi.getTrinketComponent(entity).flatMap(component -> component.getEquipped(filter).stream().findFirst().map(pair -> {
            var reference = pair.getA();
            var type = reference.inventory().getSlotType();
            return new SlotResult(new SlotContext(type.getGroup() + "/" + type.getName(), reference.index()), pair.getB());
        }));
    }

    public static ItemStack findFirst(LivingEntity entity, Predicate<ItemStack> filter) {
        return findFirstResult(entity, filter).map(SlotResult::stack).orElse(ItemStack.EMPTY);
    }

    public static ItemStack getStack(LivingEntity entity, String slotType, int slot) {
        int separator = slotType.indexOf('/');
        if (separator <= 0 || separator == slotType.length() - 1 || slot < 0) {
            return ItemStack.EMPTY;
        }
        return TrinketsApi.getTrinketComponent(entity).map(component -> {
            var group = component.getInventory().get(slotType.substring(0, separator));
            var inventory = group == null ? null : group.get(slotType.substring(separator + 1));
            return inventory == null || slot >= inventory.getContainerSize() ? ItemStack.EMPTY : inventory.getItem(slot);
        }).orElse(ItemStack.EMPTY);
    }

    public record SlotContext(String identifier, int index) {
    }

    public record SlotResult(SlotContext slotContext, ItemStack stack) {
    }
}
