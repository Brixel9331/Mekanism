package mekanism.common;

import mekanism.api.security.ISecurityUtils;
import mekanism.common.content.qio.IQIOCraftingWindowHolder;
import mekanism.common.content.qio.QIOGlobalItemLookup;
import mekanism.common.inventory.container.item.PortableQIODashboardContainer;
import mekanism.common.lib.frequency.FrequencyManager;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.lib.radiation.RadiationManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CommonWorldTickHandler {

    public static boolean flushTagAndRecipeCaches;

    private boolean registered;

    public void register() {
        if (!registered) {
            PlayerBlockBreakEvents.BEFORE.register(this::onBlockBreak);
            ServerWorldEvents.LOAD.register((server, world) -> worldLoad());
            ServerTickEvents.END_WORLD_TICK.register(this::tickEnd);
            ServerTickEvents.END_SERVER_TICK.register(server -> serverTick());
            ServerLifecycleEvents.SERVER_STOPPED.register(server -> flushTagAndRecipeCaches = false);
            registered = true;
        }
    }

    private boolean onBlockBreak(Level world, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        //Skip empty block, shouldn't be a null state but the BreakEvent still handles that as the empty block,
        // so we need to skip handling it that way, AND skip and blocks that can never have a block entity
        if (state != null && !state.isAir() && state.hasBlockEntity()) {
            //If the block might have a block entity, look it up from the world and see if the player has access to destroy it
            if (!ISecurityUtils.INSTANCE.canAccess(player, blockEntity)) {
                //If they don't because it is something that is locked, then cancel the event
                return false;
            }
        }
        return true;
    }

    private void worldLoad() {
        FrequencyManager.load();
        MultiblockManager.createOrLoadAll();
        QIOGlobalItemLookup.INSTANCE.createOrLoad();
        RadiationManager.get().createOrLoad();
    }

    private void serverTick() {
        FrequencyManager.tick();
        RadiationManager.get().tickServer();
        flushTagAndRecipeCaches = false;
    }

    private void tickEnd(ServerLevel world) {
        if (!world.isClientSide) {
            RadiationManager.get().tickServerWorld(world);
            if (flushTagAndRecipeCaches) {
                //Loop all open containers and if it is a portable qio dashboard force refresh the window's recipes
                for (ServerPlayer player : world.players()) {
                    if (player.containerMenu instanceof PortableQIODashboardContainer qioDashboard) {
                        for (byte index = 0; index < IQIOCraftingWindowHolder.MAX_CRAFTING_WINDOWS; index++) {
                            qioDashboard.getCraftingWindow(index).invalidateRecipe();
                        }
                    }
                }
            }
        }
    }
}
