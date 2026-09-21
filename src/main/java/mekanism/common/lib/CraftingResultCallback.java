package mekanism.common.lib;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface CraftingResultCallback {

    Event<CraftingResultCallback> EVENT = EventFactory.createArrayBacked(CraftingResultCallback.class, callbacks -> (result, craftingMatrix) -> {
        for (CraftingResultCallback callback : callbacks) {
            callback.onCrafting(result, craftingMatrix);
        }
    });

    void onCrafting(ItemStack result, Container craftingMatrix);
}
