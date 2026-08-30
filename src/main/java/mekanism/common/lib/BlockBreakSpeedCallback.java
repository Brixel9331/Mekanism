package mekanism.common.lib;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

@FunctionalInterface
public interface BlockBreakSpeedCallback {

    Event<BlockBreakSpeedCallback> EVENT = EventFactory.createArrayBacked(BlockBreakSpeedCallback.class, listeners -> (player, state, pos, speed) -> {
        for (BlockBreakSpeedCallback listener : listeners) {
            speed = listener.modifySpeed(player, state, pos, speed);
        }
        return Math.max(0, speed);
    });

    float modifySpeed(Player player, BlockState state, BlockPos pos, float speed);
}
