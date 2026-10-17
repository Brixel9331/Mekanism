package mekanism.common;

import mekanism.api.text.EnumColor;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.block.BlockBounding;
import mekanism.common.block.BlockCardboardBox;
import mekanism.common.block.BlockMekanism;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.item.interfaces.IItemUseFirst;
import mekanism.common.lib.radiation.capability.RadiationEntityAccess;
import mekanism.common.network.to_client.PacketPlayerData;
import mekanism.common.network.to_client.PacketRadiationData;
import mekanism.common.network.to_client.PacketResetPlayerClient;
import mekanism.common.network.to_client.PacketSecurityUpdate;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tags.MekanismTags.Items;
import mekanism.common.util.CapabilityUtils;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;

public class CommonPlayerTracker {

    private static final Component ALPHA_WARNING = MekanismLang.LOG_FORMAT.translateColored(EnumColor.RED, MekanismLang.MEKANISM, EnumColor.GRAY,
          MekanismLang.ALPHA_WARNING.translate(EnumColor.INDIGO, ChatFormatting.UNDERLINE, new ClickEvent(Action.OPEN_URL,
                "https://github.com/mekanism/Mekanism#alpha-status"), MekanismLang.ALPHA_WARNING_HERE));

    public CommonPlayerTracker() {
        Capabilities.RADIATION_ENTITY.entity().registerFallback((entity, side) -> entity instanceof RadiationEntityAccess access ? access.mekanism$getRadiation() : null);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> onPlayerLoginEvent(handler.player));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> onPlayerLogoutEvent(handler.player));
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> onPlayerDimChangedEvent(player));
        EntityTrackingEvents.START_TRACKING.register(this::onPlayerStartTrackingEvent);
        ServerPlayerEvents.COPY_FROM.register(this::cloneEvent);
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, player, alive) -> respawnEvent(player, alive));
        UseBlockCallback.EVENT.register(this::rightClickEvent);
    }

    public void onPlayerLoginEvent(ServerPlayer player) {
        if (!player.level().isClientSide) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            Mekanism.packetHandler().sendTo(new PacketSecurityUpdate(), serverPlayer);
            //serverPlayer.sendSystemMessage(ALPHA_WARNING);
            MekanismCriteriaTriggers.LOGGED_IN.trigger(serverPlayer);
        }
    }

    public void onPlayerLogoutEvent(ServerPlayer player) {
        Mekanism.playerState.clearPlayer(player.getUUID(), false);
        Mekanism.playerState.clearPlayerServerSideOnly(player.getUUID());
    }

    public void onPlayerDimChangedEvent(ServerPlayer player) {
        Mekanism.playerState.clearPlayer(player.getUUID(), false);
        Mekanism.playerState.reapplyServerSideOnly(player);
        CapabilityUtils.getCapability(player, Capabilities.RADIATION_ENTITY).ifPresent(c -> Mekanism.packetHandler().sendTo(PacketRadiationData.createPlayer(c.getRadiation()), player));
        RadiationManager.get().updateClientRadiation(player);
    }

    public void onPlayerStartTrackingEvent(Entity target, ServerPlayer serverPlayer) {
        if (target instanceof Player player) {
            Mekanism.packetHandler().sendTo(new PacketPlayerData(player.getUUID()), serverPlayer);
        }
    }

    public void cloneEvent(ServerPlayer original, ServerPlayer replacement, boolean alive) {
        if (original instanceof RadiationEntityAccess source && replacement instanceof RadiationEntityAccess target) {
            target.mekanism$copyRadiation(source);
        }
    }

    public void respawnEvent(ServerPlayer player, boolean alive) {
        CapabilityUtils.getCapability(player, Capabilities.RADIATION_ENTITY).ifPresent(c -> {
            if (!alive) {
                //If the player is returning from the end don't reset radiation
                c.set(RadiationManager.BASELINE);
            }
            Mekanism.packetHandler().sendTo(PacketRadiationData.createPlayer(c.getRadiation()), player);
        });
        RadiationManager.get().updateClientRadiation(player);
        Mekanism.packetHandler().sendToAll(new PacketResetPlayerClient(player.getUUID()));
    }

    /**
     * If the player is sneaking and the dest block is a cardboard box, ensure onBlockActivated is called, and that the item use is not.
     */
    public InteractionResult rightClickEvent(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
        if (player.isSpectator()) {
            return InteractionResult.PASS;
        }
        ItemStack itemInHand = player.getItemInHand(hand);
        if (itemInHand.is(Items.CONFIGURATORS) && !itemInHand.is(MekanismItems.CONFIGURATOR.asItem())) {
            //it's a wrench, see if it's our block. Not the configurator, as it handles bypass correctly
            Block block = level.getBlockState(hit.getBlockPos()).getBlock();
            if (player.isShiftKeyDown() && (block instanceof BlockMekanism || block instanceof BlockBounding)) {
                return level.getBlockState(hit.getBlockPos()).use(level, player, hand, hit);
            }
        } else if (player.isShiftKeyDown() && level.getBlockState(hit.getBlockPos()).getBlock() instanceof BlockCardboardBox) {
            InteractionResult result = level.getBlockState(hit.getBlockPos()).use(level, player, hand, hit);
            return result == InteractionResult.PASS ? InteractionResult.FAIL : result;
        }
        if (!itemInHand.isEmpty() && itemInHand.getItem() instanceof IItemUseFirst item &&
            !player.getCooldowns().isOnCooldown(itemInHand.getItem()) && level.getBlockState(hit.getBlockPos()).getBlock().isEnabled(level.enabledFeatures())) {
            return item.onItemUseFirst(itemInHand, new UseOnContext(player, hand, hit));
        }
        return InteractionResult.PASS;
    }
}
