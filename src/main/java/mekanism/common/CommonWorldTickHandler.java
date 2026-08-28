package mekanism.common;

import java.util.function.Predicate;
import mekanism.api.security.ISecurityUtils;
import mekanism.common.content.qio.IQIOCraftingWindowHolder;
import mekanism.common.content.qio.QIOGlobalItemLookup;
import mekanism.common.inventory.container.item.PortableQIODashboardContainer;
import mekanism.common.lib.frequency.FrequencyManager;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.util.WorldUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.Nullable;

public class CommonWorldTickHandler {

    public static boolean flushTagAndRecipeCaches;
    public static boolean monitoringCardboardBox;
    @Nullable
    public static Predicate<ItemStack> fallbackItemCollector;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntitySpawn(EntityJoinLevelEvent event) {
        //If we are in the middle of breaking a block using a cardboard box, cancel any items
        // that are dropped, we do this at highest priority to ensure we cancel it the same tick
        // before forge replaces items with custom item entities with a tick delay
        // We also cancel any experience orbs from spawning as things like the furnace will store
        // how much xp they have but also try to drop it on replace
        if (monitoringCardboardBox) {
            Entity entity = event.getEntity();
            if (entity instanceof ItemEntity || entity instanceof ExperienceOrb) {
                entity.discard();
                event.setCanceled(true);
            }
        } else if (fallbackItemCollector != null && event.getEntity() instanceof ItemEntity entity && fallbackItemCollector.test(entity.getItem())) {
            //If we have a fallback item collector active and the entity that is being added is an item,
            // try to let our fallback collector handle the item and keep track of it instead of actually adding it to the world
            entity.discard();
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        BlockState state = event.getState();
        //Skip empty block, shouldn't be a null state but the BreakEvent still handles that as the empty block,
        // so we need to skip handling it that way, AND skip and blocks that can never have a block entity
        if (state != null && !state.isAir() && state.hasBlockEntity()) {
            //If the block might have a block entity, look it up from the world and see if the player has access to destroy it
            BlockEntity blockEntity = WorldUtils.getTileEntity(event.getLevel(), event.getPos());
            if (!ISecurityUtils.INSTANCE.canAccess(event.getPlayer(), blockEntity)) {
                //If they don't because it is something that is locked, then cancel the event
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void worldLoadEvent(LevelEvent.Load event) {
        if (!event.getLevel().isClientSide()) {
            FrequencyManager.load();
            MultiblockManager.createOrLoadAll();
            QIOGlobalItemLookup.INSTANCE.createOrLoad();
            RadiationManager.get().createOrLoad();
        }
    }

    @SubscribeEvent
    public void onTick(ServerTickEvent event) {
        if (event.side.isServer() && event.phase == Phase.END) {
            serverTick();
        }
    }

    @SubscribeEvent
    public void onTick(LevelTickEvent event) {
        if (event.side.isServer() && event.phase == Phase.END) {
            tickEnd((ServerLevel) event.level);
        }
    }

    private void serverTick() {
        FrequencyManager.tick();
        RadiationManager.get().tickServer();
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
                flushTagAndRecipeCaches = false;
            }
        }
    }
}
