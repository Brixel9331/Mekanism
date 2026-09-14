package mekanism.common.lib;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface EntityBlockPlaceCallback {

    Event<EntityBlockPlaceCallback> EVENT = EventFactory.createArrayBacked(EntityBlockPlaceCallback.class,
          listeners -> (world, entity, pos, face, originalState, newState) -> {
              for (EntityBlockPlaceCallback listener : listeners) {
                  if (!listener.canPlace(world, entity, pos, face, originalState, newState)) {
                      return false;
                  }
              }
              return true;
          });

    boolean canPlace(Level world, @Nullable Entity entity, BlockPos pos, Direction face, BlockState originalState, BlockState newState);
}
