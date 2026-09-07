package mekanism.client;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.radial.RadialData;
import mekanism.client.gui.GuiRadialSelector;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MouseScrollCallback;
import mekanism.client.render.FogRenderCallback;
import mekanism.client.render.FogRenderCallback.FogDistance;
import mekanism.client.render.LivingModelRenderCallback;
import mekanism.client.render.ModelVisibility;
import mekanism.client.render.VisionEnhancement;
import mekanism.client.render.hud.MekanismStatusOverlay;
import mekanism.client.render.lib.ScrollIncrementer;
import mekanism.client.sound.GeigerSound;
import mekanism.client.sound.SoundHandler;
import mekanism.common.CommonPlayerTickHandler;
import mekanism.common.Mekanism;
import mekanism.common.base.HolidayManager;
import mekanism.common.base.KeySync;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.mekasuit.ModuleVisionEnhancementUnit;
import mekanism.common.item.gear.ItemFlamethrower;
import mekanism.common.item.gear.ItemHDPEElytra;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.item.interfaces.IJetpackItem;
import mekanism.common.item.interfaces.IJetpackItem.JetpackMode;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.radial.IGenericRadialModeItem;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.lib.radiation.RadiationManager.RadiationScale;
import mekanism.common.network.to_server.PacketModeChange;
import mekanism.common.network.to_server.PacketPortableTeleporterTeleport;
import mekanism.common.registries.MekanismModules;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.MekanismUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmorStandModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.FogRenderer.FogMode;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

/**
 * Client-side tick handler for Mekanism. Used mainly for the update check upon startup.
 *
 * @author AidanBrady
 */
public class ClientTickHandler {

    public static final Minecraft minecraft = Minecraft.getInstance();
    public static final Random rand = new Random();
    public static final Map<Player, TeleportData> portableTeleports = new Object2ObjectArrayMap<>(1);
    private static final ScrollIncrementer scrollIncrementer = new ScrollIncrementer(true);

    public static boolean firstTick = true;
    public static boolean visionEnhancement = false;

    public boolean initHoliday = false;
    public boolean shouldReset = false;

    public static boolean isJetpackInUse(Player player, ItemStack jetpack) {
        if (!player.isSpectator() && !jetpack.isEmpty()) {
            JetpackMode mode = ((IJetpackItem) jetpack.getItem()).getJetpackMode(jetpack);
            boolean guiOpen = minecraft.screen != null;
            boolean ascending = minecraft.player.input.jumping;
            boolean rising = ascending && !guiOpen;
            if (mode == JetpackMode.NORMAL) {
                return rising;
            } else if (mode == JetpackMode.HOVER) {
                boolean descending = minecraft.player.input.shiftKeyDown;
                if (!rising || descending) {
                    return !CommonPlayerTickHandler.isOnGroundOrSleeping(player);
                }
                return true;
            }
        }
        return false;
    }

    public static boolean isScubaMaskOn(Player player) {
        if (player != minecraft.player) {
            return Mekanism.playerState.isScubaMaskOn(player);
        }
        return CommonPlayerTickHandler.isScubaMaskOn(player, player.getItemBySlot(EquipmentSlot.CHEST));
    }

    public static boolean isGravitationalModulationOn(Player player) {
        if (player != minecraft.player) {
            return Mekanism.playerState.isGravitationalModulationOn(player);
        }
        return CommonPlayerTickHandler.isGravitationalModulationOn(player);
    }

    public static boolean isVisionEnhancementOn(Player player) {
        IModule<ModuleVisionEnhancementUnit> module = IModuleHelper.INSTANCE.load(player.getItemBySlot(EquipmentSlot.HEAD), MekanismModules.VISION_ENHANCEMENT_UNIT);
        return module != null && module.isEnabled() && module.hasEnoughEnergy(MekanismConfig.gear.mekaSuitEnergyUsageVisionEnhancement);
    }

    public static boolean isFlamethrowerOn(Player player) {
        if (player != minecraft.player) {
            return Mekanism.playerState.isFlamethrowerOn(player);
        }
        return hasFlamethrower(player) && minecraft.options.keyUse.isDown();
    }

    public static boolean hasFlamethrower(Player player) {
        ItemStack currentItem = player.getInventory().getSelected();
        return !currentItem.isEmpty() && currentItem.getItem() instanceof ItemFlamethrower && ChemicalUtil.hasGas(currentItem);
    }

    public static void portableTeleport(Player player, InteractionHand hand, FrequencyIdentity identity) {
        int delay = MekanismConfig.gear.portableTeleporterDelay.get();
        if (delay == 0) {
            Mekanism.packetHandler().sendToServer(new PacketPortableTeleporterTeleport(hand, identity));
        } else {
            portableTeleports.put(player, new TeleportData(hand, identity, minecraft.level.getGameTime() + delay));
        }
    }

    public static void register() {
        LivingModelRenderCallback.EVENT.register(ClientTickHandler::hideModelParts);
        FogRenderCallback.COLOR.register(ClientTickHandler::onFogLighting);
        FogRenderCallback.DISTANCE.register(ClientTickHandler::onFog);
        HolidayManager.init();
        ClientTickHandler handler = new ClientTickHandler();
        ClientTickEvents.START_CLIENT_TICK.register(client -> handler.tickStart());
        MouseScrollCallback.EVENT.register(handler::onMouseScroll);
    }

    public void tickStart() {
        MekanismClient.ticksPassed++;

        if (firstTick && minecraft.level != null) {
            MekanismClient.launchClient();
            firstTick = false;
        }

        if (minecraft.level != null) {
            shouldReset = true;
        } else if (shouldReset) {
            MekanismClient.reset();
            shouldReset = false;
        }

        if (minecraft.level != null && minecraft.player != null && !minecraft.isPaused()) {
            if (!initHoliday || MekanismClient.ticksPassed % 1_200 == 0) {
                HolidayManager.notify(Minecraft.getInstance().player);
                initHoliday = true;
            }

            //Reboot player sounds if needed
            SoundHandler.restartSounds();

            RadiationManager.get().tickClient(minecraft.player);

            UUID playerUUID = minecraft.player.getUUID();
            // Update player's state for various items; this also automatically notifies server if something changed and
            // kicks off sounds as necessary
            ItemStack jetpack = IJetpackItem.getActiveJetpack(minecraft.player);
            boolean jetpackInUse = isJetpackInUse(minecraft.player, jetpack);
            Mekanism.playerState.setJetpackState(playerUUID, jetpackInUse, true);
            Mekanism.playerState.setScubaMaskState(playerUUID, isScubaMaskOn(minecraft.player), true);
            Mekanism.playerState.setGravitationalModulationState(playerUUID, isGravitationalModulationOn(minecraft.player), true);
            Mekanism.playerState.setFlamethrowerState(playerUUID, hasFlamethrower(minecraft.player), isFlamethrowerOn(minecraft.player), true);

            for (Iterator<Entry<Player, TeleportData>> iter = portableTeleports.entrySet().iterator(); iter.hasNext(); ) {
                Entry<Player, TeleportData> entry = iter.next();
                Player player = entry.getKey();
                for (int i = 0; i < 100; i++) {
                    double x = player.getX() + rand.nextDouble() - 0.5D;
                    double y = player.getY() + rand.nextDouble() * 2 - 2D;
                    double z = player.getZ() + rand.nextDouble() - 0.5D;
                    minecraft.level.addParticle(ParticleTypes.PORTAL, x, y, z, 0, 1, 0);
                }
                TeleportData data = entry.getValue();
                if (minecraft.level.getGameTime() == data.teleportTime) {
                    Mekanism.packetHandler().sendToServer(new PacketPortableTeleporterTeleport(data.hand, data.identity));
                    iter.remove();
                }
            }

            if (!jetpack.isEmpty()) {
                ItemStack primaryJetpack = IJetpackItem.getPrimaryJetpack(minecraft.player);
                if (!primaryJetpack.isEmpty()) {
                    JetpackMode primaryMode = ((IJetpackItem) primaryJetpack.getItem()).getJetpackMode(primaryJetpack);
                    JetpackMode mode = IJetpackItem.getPlayerJetpackMode(minecraft.player, primaryMode, () -> minecraft.player.input.jumping);
                    MekanismClient.updateKey(minecraft.player.input.jumping, KeySync.ASCEND);
                    if (jetpackInUse && IJetpackItem.handleJetpackMotion(minecraft.player, mode, () -> minecraft.player.input.jumping)) {
                        minecraft.player.resetFallDistance();
                    }
                }
            }

            if (isScubaMaskOn(minecraft.player) && minecraft.player.getAirSupply() == minecraft.player.getMaxAirSupply()) {
                for (MobEffectInstance effect : minecraft.player.getActiveEffects()) {
                    if (MekanismUtils.shouldSpeedUpEffect(effect)) {
                        for (int i = 0; i < 9; i++) {
                            MekanismUtils.speedUpEffectSafely(minecraft.player, effect);
                        }
                    }
                }
            }

            if (isVisionEnhancementOn(minecraft.player)) {
                visionEnhancement = true;
                // adds if it doesn't exist, otherwise tops off duration to 220. equal or less than 200 will make vision flickers
                minecraft.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 220, 0, false, false, false));
            } else if (visionEnhancement) {
                visionEnhancement = false;
                MobEffectInstance effect = minecraft.player.getEffect(MobEffects.NIGHT_VISION);
                if (effect != null && effect.getDuration() <= 220) {
                    //Only remove it if it is our effect and not one that has a longer remaining duration
                    minecraft.player.removeEffect(MobEffects.NIGHT_VISION);
                }
            }

            if (minecraft.screen == null || minecraft.screen instanceof GuiRadialSelector) {
                if (!MekKeyHandler.isRadialPressed() || (!updateSelectorRenderer(EquipmentSlot.MAINHAND) && !updateSelectorRenderer(EquipmentSlot.OFFHAND))) {
                    if (minecraft.screen != null) {
                        //If we currently have a radial selector gui open but shouldn't close it
                        minecraft.setScreen(null);
                    }
                }
            }

            if (MekanismConfig.client.enablePlayerSounds.get()) {
                RadiationScale scale = RadiationManager.get().getClientScale();
                if (scale != RadiationScale.NONE && !SoundHandler.radiationSoundMap.containsKey(scale)) {
                    GeigerSound sound = GeigerSound.create(minecraft.player, scale);
                    SoundHandler.radiationSoundMap.put(scale, sound);
                    SoundHandler.playSound(sound);
                }
            }
        }
    }

    private boolean updateSelectorRenderer(EquipmentSlot slot) {
        if (minecraft.player != null) {
            ItemStack stack = minecraft.player.getItemBySlot(slot);
            if (stack.getItem() instanceof IGenericRadialModeItem item) {
                RadialData<?> radialData = item.getRadialData(stack);
                if (radialData != null) {
                    if (!(minecraft.screen instanceof GuiRadialSelector screen) || !screen.hasMatchingData(slot, radialData)) {
                        GuiRadialSelector newSelector = new GuiRadialSelector(slot, radialData, () -> minecraft.player);
                        newSelector.tryInheritCurrentPath(minecraft.screen);
                        minecraft.setScreen(newSelector);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private boolean onMouseScroll(Minecraft client, double delta) {
        if (MekanismConfig.client.allowModeScroll.get() && client.player != null && client.player.isShiftKeyDown()) {
            return handleModeScroll(EquipmentSlot.MAINHAND, delta);
        }
        return false;
    }

    private boolean handleModeScroll(EquipmentSlot slot, double delta) {
        if (delta != 0 && IModeItem.isModeItem(minecraft.player, slot)) {
            int shift = scrollIncrementer.scroll(delta);
            if (shift != 0) {
                MekanismStatusOverlay.INSTANCE.setTimer();
                Mekanism.packetHandler().sendToServer(new PacketModeChange(slot, shift));
            }
            return true;
        }
        return false;
    }

    private static void onFogLighting(Camera camera, float partialTick, Vector3f color) {
        if (visionEnhancement) {
            VisionEnhancement.adjustColor(color);
        }
    }

    private static FogDistance onFog(Camera camera, FogMode mode, float partialTick, FogDistance distance) {
        if (visionEnhancement && camera.getEntity() instanceof Player player) {
            IModule<ModuleVisionEnhancementUnit> module = IModuleHelper.INSTANCE.load(player.getItemBySlot(EquipmentSlot.HEAD), MekanismModules.VISION_ENHANCEMENT_UNIT);
            //Double-check the module is present before doing any calculations. This should always be true here, but better safe than sorry
            if (module != null) {
                return VisionEnhancement.adjustDistance(camera.getFluidInCamera(), distance, module.getInstalledCount(), module.getData().getMaxStackSize());
            }
        }
        return distance;
    }

    private static void hideModelParts(LivingEntity entity, EntityModel<?> model, ModelVisibility visibility) {
        if (model instanceof HumanoidModel<?> humanoidModel) {
            //If the entity has a biped model, then see if it is wearing a meka suit, in which case we want to hide various parts of the model
            setModelVisibility(entity, humanoidModel, visibility);
        }
    }

    private static void setModelVisibility(LivingEntity entity, HumanoidModel<?> entityModel, ModelVisibility visibility) {
        if (entity.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof ItemMekaSuitArmor) {
            visibility.hide(entityModel.head);
            visibility.hide(entityModel.hat);
            if (entityModel instanceof PlayerModel<?> playerModel) {
                visibility.hide(playerModel.ear);
            }
        }
        ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getItem() instanceof ItemMekaSuitArmor) {
            visibility.hide(entityModel.body);
            if (!(entity instanceof ArmorStand)) {
                //Don't adjust arms for armor stands as the model will end up changing them anyway and then we may incorrectly activate them
                visibility.hide(entityModel.leftArm);
                visibility.hide(entityModel.rightArm);
            }
            if (entityModel instanceof PlayerModel<?> playerModel) {
                visibility.hide(playerModel.cloak);
                visibility.hide(playerModel.jacket);
                visibility.hide(playerModel.leftSleeve);
                visibility.hide(playerModel.rightSleeve);
            } else if (entityModel instanceof ArmorStandModel armorStandModel) {
                visibility.hide(armorStandModel.rightBodyStick);
                visibility.hide(armorStandModel.leftBodyStick);
                visibility.hide(armorStandModel.shoulderStick);
            }
        } else if (chest.getItem() instanceof ItemHDPEElytra && entityModel instanceof PlayerModel<?> playerModel) {
            //Hide the player's cape if they have an HDPE elytra as it will be part of the elytra's layer and shouldn't be rendered
            visibility.hide(playerModel.cloak);
        }
        if (entity.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof ItemMekaSuitArmor) {
            visibility.hide(entityModel.leftLeg);
            visibility.hide(entityModel.rightLeg);
            if (entityModel instanceof PlayerModel<?> playerModel) {
                visibility.hide(playerModel.leftPants);
                visibility.hide(playerModel.rightPants);
            }
        }
    }

    private record TeleportData(InteractionHand hand, FrequencyIdentity identity, long teleportTime) {
    }
}
