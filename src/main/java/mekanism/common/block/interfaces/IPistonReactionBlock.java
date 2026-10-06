package mekanism.common.block.interfaces;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;

public interface IPistonReactionBlock {

    PushReaction getPistonPushReaction(BlockState state, PushReaction original);
}
