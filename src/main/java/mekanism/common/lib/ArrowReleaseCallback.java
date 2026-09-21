package mekanism.common.lib;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@FunctionalInterface
public interface ArrowReleaseCallback {

    Event<ArrowReleaseCallback> EVENT = EventFactory.createArrayBacked(ArrowReleaseCallback.class, callbacks -> (stack, level, player, charge, hasAmmo) -> {
        for (ArrowReleaseCallback callback : callbacks) {
            charge = callback.release(stack, level, player, charge, hasAmmo);
            if (charge < 0) {
                break;
            }
        }
        return charge;
    });

    int release(ItemStack stack, Level level, Player player, int charge, boolean hasAmmo);
}
