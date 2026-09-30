package mekanism.client;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import com.google.common.collect.Table.Cell;
import java.util.Map.Entry;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.providers.IItemProvider;
import mekanism.api.text.EnumColor;
import mekanism.api.tier.BaseTier;
import mekanism.client.gui.GuiBoilerStats;
import mekanism.client.gui.GuiChemicalTank;
import mekanism.client.gui.GuiDimensionalStabilizer;
import mekanism.client.gui.GuiDynamicTank;
import mekanism.client.gui.GuiEnergyCube;
import mekanism.client.gui.GuiFluidTank;
import mekanism.client.gui.GuiInductionMatrix;
import mekanism.client.gui.GuiLaserAmplifier;
import mekanism.client.gui.GuiLaserTractorBeam;
import mekanism.client.gui.GuiLogisticalSorter;
import mekanism.client.gui.GuiMatrixStats;
import mekanism.client.gui.GuiModificationStation;
import mekanism.client.gui.GuiModuleTweaker;
import mekanism.client.gui.GuiPersonalStorageTile;
import mekanism.client.gui.GuiQuantumEntangloporter;
import mekanism.client.gui.GuiRadialSelector;
import mekanism.client.gui.GuiSPS;
import mekanism.client.gui.GuiSecurityDesk;
import mekanism.client.gui.GuiTeleporter;
import mekanism.client.gui.GuiThermalEvaporationController;
import mekanism.client.gui.GuiThermoelectricBoiler;
import mekanism.client.gui.item.GuiDictionary;
import mekanism.client.gui.item.GuiPersonalStorageItem;
import mekanism.client.gui.item.GuiPortableTeleporter;
import mekanism.client.gui.item.GuiSeismicReader;
import mekanism.client.gui.machine.GuiAntiprotonicNucleosynthesizer;
import mekanism.client.gui.machine.GuiChemicalCrystallizer;
import mekanism.client.gui.machine.GuiChemicalDissolutionChamber;
import mekanism.client.gui.machine.GuiChemicalInfuser;
import mekanism.client.gui.machine.GuiChemicalOxidizer;
import mekanism.client.gui.machine.GuiChemicalWasher;
import mekanism.client.gui.machine.GuiCombiner;
import mekanism.client.gui.machine.GuiDigitalMiner;
import mekanism.client.gui.machine.GuiDigitalMinerConfig;
import mekanism.client.gui.machine.GuiElectricPump;
import mekanism.client.gui.machine.GuiElectrolyticSeparator;
import mekanism.client.gui.machine.GuiFactory;
import mekanism.client.gui.machine.GuiFluidicPlenisher;
import mekanism.client.gui.machine.GuiFormulaicAssemblicator;
import mekanism.client.gui.machine.GuiFuelwoodHeater;
import mekanism.client.gui.machine.GuiIsotopicCentrifuge;
import mekanism.client.gui.machine.GuiMetallurgicInfuser;
import mekanism.client.gui.machine.GuiNutritionalLiquifier;
import mekanism.client.gui.machine.GuiOredictionificator;
import mekanism.client.gui.machine.GuiPRC;
import mekanism.client.gui.machine.GuiPaintingMachine;
import mekanism.client.gui.machine.GuiPigmentExtractor;
import mekanism.client.gui.machine.GuiPigmentMixer;
import mekanism.client.gui.machine.GuiPrecisionSawmill;
import mekanism.client.gui.machine.GuiResistiveHeater;
import mekanism.client.gui.machine.GuiRotaryCondensentrator;
import mekanism.client.gui.machine.GuiSeismicVibrator;
import mekanism.client.gui.machine.GuiSolarNeutronActivator;
import mekanism.client.gui.qio.GuiPortableQIODashboard;
import mekanism.client.gui.qio.GuiQIODashboard;
import mekanism.client.gui.qio.GuiQIODriveArray;
import mekanism.client.gui.qio.GuiQIOExporter;
import mekanism.client.gui.qio.GuiQIOImporter;
import mekanism.client.gui.qio.GuiQIOItemFrequencySelect;
import mekanism.client.gui.qio.GuiQIORedstoneAdapter;
import mekanism.client.gui.qio.GuiQIOTileFrequencySelect;
import mekanism.client.gui.robit.GuiRobitCrafting;
import mekanism.client.gui.robit.GuiRobitInventory;
import mekanism.client.gui.robit.GuiRobitMain;
import mekanism.client.gui.robit.GuiRobitRepair;
import mekanism.client.gui.robit.GuiRobitSmelting;
import mekanism.client.model.MekanismModelCache;
import mekanism.client.model.ModelArmoredFreeRunners;
import mekanism.client.model.ModelArmoredJetpack;
import mekanism.client.model.ModelAtomicDisassembler;
import mekanism.client.model.ModelEnergyCore;
import mekanism.client.model.ModelFlamethrower;
import mekanism.client.model.ModelFreeRunners;
import mekanism.client.model.ModelIndustrialAlarm;
import mekanism.client.model.ModelJetpack;
import mekanism.client.model.ModelScubaMask;
import mekanism.client.model.ModelScubaTank;
import mekanism.client.model.ModelTransporterBox;
import mekanism.client.model.baked.DigitalMinerBakedModel;
import mekanism.client.model.baked.ExtensionBakedModel.LightedBakedModel;
import mekanism.client.particle.JetpackFlameParticle;
import mekanism.client.particle.JetpackSmokeParticle;
import mekanism.client.particle.LaserParticle;
import mekanism.client.particle.RadiationParticle;
import mekanism.client.particle.ScubaBubbleParticle;
import mekanism.client.render.FluidRenderRegistration;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.armor.FreeRunnerArmor;
import mekanism.client.render.armor.JetpackArmor;
import mekanism.client.render.armor.ScubaMaskArmor;
import mekanism.client.render.armor.ScubaTankArmor;
import mekanism.client.render.entity.RenderFlame;
import mekanism.client.render.entity.RenderRobit;
import mekanism.client.render.hud.HudRenderCallback;
import mekanism.client.render.hud.MekaSuitEnergyLevel;
import mekanism.client.render.hud.MekanismHUD;
import mekanism.client.render.hud.MekanismStatusOverlay;
import mekanism.client.render.hud.RadiationOverlay;
import mekanism.client.render.item.ItemDecorationRegistry;
import mekanism.client.render.item.MekaSuitBarDecorator;
import mekanism.client.render.item.TransmitterTypeDecorator;
import mekanism.client.render.item.block.RenderEnergyCubeItem;
import mekanism.client.render.item.gear.RenderAtomicDisassembler;
import mekanism.client.render.item.gear.RenderFlameThrower;
import mekanism.client.render.item.gear.RenderFreeRunners;
import mekanism.client.render.item.gear.RenderJetpack;
import mekanism.client.render.item.gear.RenderScubaMask;
import mekanism.client.render.item.gear.RenderScubaTank;
import mekanism.client.render.layer.MekanismArmorLayer;
import mekanism.client.render.layer.MekanismElytraLayer;
import mekanism.client.render.obj.TransmitterLoader;
import mekanism.client.render.tileentity.RenderBin;
import mekanism.client.render.tileentity.RenderDigitalMiner;
import mekanism.client.render.tileentity.RenderDimensionalStabilizer;
import mekanism.client.render.tileentity.RenderDynamicTank;
import mekanism.client.render.tileentity.RenderEnergyCube;
import mekanism.client.render.tileentity.RenderFluidTank;
import mekanism.client.render.tileentity.RenderIndustrialAlarm;
import mekanism.client.render.tileentity.RenderNutritionalLiquifier;
import mekanism.client.render.tileentity.RenderPersonalChest;
import mekanism.client.render.tileentity.RenderPigmentMixer;
import mekanism.client.render.tileentity.RenderSPS;
import mekanism.client.render.tileentity.RenderSeismicVibrator;
import mekanism.client.render.tileentity.RenderTeleporter;
import mekanism.client.render.tileentity.RenderThermalEvaporationPlant;
import mekanism.client.render.tileentity.RenderThermoelectricBoiler;
import mekanism.client.render.transmitter.RenderLogisticalTransporter;
import mekanism.client.render.transmitter.RenderMechanicalPipe;
import mekanism.client.render.transmitter.RenderPressurizedTube;
import mekanism.client.render.transmitter.RenderThermodynamicConductor;
import mekanism.client.render.transmitter.RenderUniversalCable;
import mekanism.common.Mekanism;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.content.gear.shared.ModuleColorModulationUnit;
import mekanism.common.item.ItemConfigurationCard;
import mekanism.common.item.ItemCraftingFormula;
import mekanism.common.item.block.ItemBlockCardboardBox;
import mekanism.common.item.block.machine.ItemBlockFluidTank;
import mekanism.common.lib.Color;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registration.impl.ItemRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.registries.MekanismEntityTypes;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismItems;
import mekanism.common.registries.MekanismModules;
import mekanism.common.registries.MekanismParticleTypes;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.resource.IResource;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tile.qio.TileEntityQIOComponent;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporter;
import mekanism.common.util.RegistryUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;
import io.github.fabricators_of_create.porting_lib.models.geometry.RegisterGeometryLoadersCallback;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.client.renderer.texture.TextureAtlas;
import java.util.Collection;
import java.util.List;

@Mod.EventBusSubscriber(modid = Mekanism.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientRegistration {

    private static final Map<ResourceLocation, CustomModelRegistryObject> customModels = new ConcurrentHashMap<>();

    public static void init() {
        IModuleHelper moduleHelper = IModuleHelper.INSTANCE;
        moduleHelper.addMekaSuitModuleModels(Mekanism.rl("models/entity/mekasuit_modules.obj"));
        moduleHelper.addMekaSuitModuleModelSpec("jetpack", MekanismModules.JETPACK_UNIT, EquipmentSlot.CHEST);
        moduleHelper.addMekaSuitModuleModelSpec("modulator", MekanismModules.GRAVITATIONAL_MODULATING_UNIT, EquipmentSlot.CHEST);
        moduleHelper.addMekaSuitModuleModelSpec("elytra", MekanismModules.ELYTRA_UNIT, EquipmentSlot.CHEST, LivingEntity::isFallFlying);

        //Set fluids to a translucent render layer
        FluidRenderRegistration.register(MekanismFluids.FLUIDS);
        ClientRegistrationUtil.setPropertyOverride(MekanismBlocks.CARDBOARD_BOX, Mekanism.rl("storage"),
              (stack, world, entity, seed) -> ((ItemBlockCardboardBox) stack.getItem()).getBlockData(world, stack) == null ? 0 : 1);

        ClientRegistrationUtil.setPropertyOverride(MekanismItems.CRAFTING_FORMULA, Mekanism.rl("invalid"), (stack, world, entity, seed) -> {
            ItemCraftingFormula formula = (ItemCraftingFormula) stack.getItem();
            return formula.hasInventory(stack) && formula.isInvalid(stack) ? 1 : 0;
        });
        ClientRegistrationUtil.setPropertyOverride(MekanismItems.CRAFTING_FORMULA, Mekanism.rl("encoded"), (stack, world, entity, seed) -> {
            ItemCraftingFormula formula = (ItemCraftingFormula) stack.getItem();
            return formula.hasInventory(stack) && !formula.isInvalid(stack) ? 1 : 0;
        });
        ClientRegistrationUtil.setPropertyOverride(MekanismItems.CONFIGURATION_CARD, Mekanism.rl("encoded"),
              (stack, world, entity, seed) -> ((ItemConfigurationCard) stack.getItem()).hasData(stack) ? 1 : 0);

        ClientRegistrationUtil.setPropertyOverride(MekanismItems.ELECTRIC_BOW, Mekanism.rl("pull"),
              (stack, world, entity, seed) -> entity != null && entity.getUseItem() == stack ? (stack.getUseDuration() - entity.getUseItemRemainingTicks()) / 20.0F : 0);
        ClientRegistrationUtil.setPropertyOverride(MekanismItems.ELECTRIC_BOW, Mekanism.rl("pulling"),
              (stack, world, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);

        ClientRegistrationUtil.setPropertyOverride(MekanismItems.GEIGER_COUNTER, Mekanism.rl("radiation"), (stack, world, entity, seed) -> {
            if (entity instanceof Player) {
                return RadiationManager.get().getClientScale().ordinal();
            }
            return 0;
        });
        //Note: Our implementation allows for a null entity so don't worry about it and pass it
        ClientRegistrationUtil.setPropertyOverride(MekanismItems.HDPE_REINFORCED_ELYTRA, Mekanism.rl("broken"), (stack, world, entity, seed) -> {
            boolean canFly;
            if (entity == null) {
                //Fallback to the vanilla check in case any mods like quark are making vanilla actually make use of the entity
                canFly = stack.getDamageValue() < stack.getMaxDamage() - 1;
            } else {
                canFly = net.minecraft.world.item.ElytraItem.isFlyEnabled(stack);
            }
            return canFly ? 0.0F : 1.0F;
        });

        addCustomModel(MekanismBlocks.DIGITAL_MINER, (orig, evt) -> new DigitalMinerBakedModel(orig));

        addLitModel(MekanismItems.MEKA_TOOL);
    }

    public static void registerOverlays() {
        HudRenderCallback.BACKGROUND.register(RadiationOverlay.INSTANCE);
        HudRenderCallback.STATUS_BARS.register(MekaSuitEnergyLevel.INSTANCE);
        //Render status overlay after item name rather than action bar (record_overlay) so that things like the sleep fade will render in front of our overlay
        HudRenderCallback.SELECTED_ITEM.register(MekanismStatusOverlay.INSTANCE);
        HudRenderCallback.HOTBAR.register(MekanismHUD.INSTANCE);
        //Hide the crosshair if we have a radial menu open and are drawing the back button
        HudRenderCallback.CROSSHAIR.register(client -> !(client.screen instanceof GuiRadialSelector screen && screen.shouldHideCrosshair()));
    }

    public static void registerRenderers() {
        //Register entity rendering handlers
        EntityRendererRegistry.register(MekanismEntityTypes.ROBIT.get(), RenderRobit::new);
        EntityRendererRegistry.register(MekanismEntityTypes.FLAME.get(), RenderFlame::new);

        //Register TileEntityRenderers
        ClientRegistrationUtil.bindTileEntityRenderer(RenderThermoelectricBoiler::new, MekanismTileEntityTypes.BOILER_CASING, MekanismTileEntityTypes.BOILER_VALVE);
        ClientRegistrationUtil.bindTileEntityRenderer(RenderDynamicTank::new, MekanismTileEntityTypes.DYNAMIC_TANK, MekanismTileEntityTypes.DYNAMIC_VALVE);
        BlockEntityRendererRegistry.register(MekanismTileEntityTypes.DIGITAL_MINER.get(), RenderDigitalMiner::new);
        BlockEntityRendererRegistry.register(MekanismTileEntityTypes.DIMENSIONAL_STABILIZER.get(), RenderDimensionalStabilizer::new);
        BlockEntityRendererRegistry.register(MekanismTileEntityTypes.PERSONAL_CHEST.get(), RenderPersonalChest::new);
        BlockEntityRendererRegistry.register(MekanismTileEntityTypes.NUTRITIONAL_LIQUIFIER.get(), RenderNutritionalLiquifier::new);
        BlockEntityRendererRegistry.register(MekanismTileEntityTypes.PIGMENT_MIXER.get(), RenderPigmentMixer::new);
        BlockEntityRendererRegistry.register(MekanismTileEntityTypes.SEISMIC_VIBRATOR.get(), RenderSeismicVibrator::new);
        BlockEntityRendererRegistry.register(MekanismTileEntityTypes.TELEPORTER.get(), RenderTeleporter::new);
        BlockEntityRendererRegistry.register(MekanismTileEntityTypes.THERMAL_EVAPORATION_CONTROLLER.get(), RenderThermalEvaporationPlant::new);
        BlockEntityRendererRegistry.register(MekanismTileEntityTypes.INDUSTRIAL_ALARM.get(), RenderIndustrialAlarm::new);
        ClientRegistrationUtil.bindTileEntityRenderer(RenderSPS::new, MekanismTileEntityTypes.SPS_CASING, MekanismTileEntityTypes.SPS_PORT);
        ClientRegistrationUtil.bindTileEntityRenderer(RenderBin::new, MekanismTileEntityTypes.BASIC_BIN, MekanismTileEntityTypes.ADVANCED_BIN, MekanismTileEntityTypes.ELITE_BIN,
              MekanismTileEntityTypes.ULTIMATE_BIN, MekanismTileEntityTypes.CREATIVE_BIN);
        ClientRegistrationUtil.bindTileEntityRenderer(RenderEnergyCube::new, MekanismTileEntityTypes.BASIC_ENERGY_CUBE, MekanismTileEntityTypes.ADVANCED_ENERGY_CUBE,
              MekanismTileEntityTypes.ELITE_ENERGY_CUBE, MekanismTileEntityTypes.ULTIMATE_ENERGY_CUBE, MekanismTileEntityTypes.CREATIVE_ENERGY_CUBE);
        ClientRegistrationUtil.bindTileEntityRenderer(RenderFluidTank::new, MekanismTileEntityTypes.BASIC_FLUID_TANK, MekanismTileEntityTypes.ADVANCED_FLUID_TANK,
              MekanismTileEntityTypes.ELITE_FLUID_TANK, MekanismTileEntityTypes.ULTIMATE_FLUID_TANK, MekanismTileEntityTypes.CREATIVE_FLUID_TANK);
        //Transmitters
        ClientRegistrationUtil.bindTileEntityRenderer(RenderLogisticalTransporter::new, MekanismTileEntityTypes.RESTRICTIVE_TRANSPORTER,
              MekanismTileEntityTypes.DIVERSION_TRANSPORTER, MekanismTileEntityTypes.BASIC_LOGISTICAL_TRANSPORTER, MekanismTileEntityTypes.ADVANCED_LOGISTICAL_TRANSPORTER,
              MekanismTileEntityTypes.ELITE_LOGISTICAL_TRANSPORTER, MekanismTileEntityTypes.ULTIMATE_LOGISTICAL_TRANSPORTER);
        ClientRegistrationUtil.bindTileEntityRenderer(RenderMechanicalPipe::new, MekanismTileEntityTypes.BASIC_MECHANICAL_PIPE,
              MekanismTileEntityTypes.ADVANCED_MECHANICAL_PIPE, MekanismTileEntityTypes.ELITE_MECHANICAL_PIPE, MekanismTileEntityTypes.ULTIMATE_MECHANICAL_PIPE);
        ClientRegistrationUtil.bindTileEntityRenderer(RenderPressurizedTube::new, MekanismTileEntityTypes.BASIC_PRESSURIZED_TUBE,
              MekanismTileEntityTypes.ADVANCED_PRESSURIZED_TUBE, MekanismTileEntityTypes.ELITE_PRESSURIZED_TUBE, MekanismTileEntityTypes.ULTIMATE_PRESSURIZED_TUBE);
        ClientRegistrationUtil.bindTileEntityRenderer(RenderUniversalCable::new, MekanismTileEntityTypes.BASIC_UNIVERSAL_CABLE,
              MekanismTileEntityTypes.ADVANCED_UNIVERSAL_CABLE, MekanismTileEntityTypes.ELITE_UNIVERSAL_CABLE, MekanismTileEntityTypes.ULTIMATE_UNIVERSAL_CABLE);
        ClientRegistrationUtil.bindTileEntityRenderer(RenderThermodynamicConductor::new, MekanismTileEntityTypes.BASIC_THERMODYNAMIC_CONDUCTOR,
              MekanismTileEntityTypes.ADVANCED_THERMODYNAMIC_CONDUCTOR, MekanismTileEntityTypes.ELITE_THERMODYNAMIC_CONDUCTOR, MekanismTileEntityTypes.ULTIMATE_THERMODYNAMIC_CONDUCTOR);
    }

    public static void registerLayer() {
        EntityModelLayerRegistry.registerModelLayer(ModelJetpack.JETPACK_LAYER, ModelJetpack::createLayerDefinition);
        EntityModelLayerRegistry.registerModelLayer(ModelArmoredJetpack.ARMORED_JETPACK_LAYER, ModelArmoredJetpack::createLayerDefinition);
        EntityModelLayerRegistry.registerModelLayer(ModelAtomicDisassembler.DISASSEMBLER_LAYER, ModelAtomicDisassembler::createLayerDefinition);
        EntityModelLayerRegistry.registerModelLayer(ModelEnergyCore.CORE_LAYER, ModelEnergyCore::createLayerDefinition);
        EntityModelLayerRegistry.registerModelLayer(ModelFlamethrower.FLAMETHROWER_LAYER, ModelFlamethrower::createLayerDefinition);
        EntityModelLayerRegistry.registerModelLayer(ModelArmoredFreeRunners.ARMORED_FREE_RUNNER_LAYER, ModelArmoredFreeRunners::createLayerDefinition);
        EntityModelLayerRegistry.registerModelLayer(ModelFreeRunners.FREE_RUNNER_LAYER, ModelFreeRunners::createLayerDefinition);
        EntityModelLayerRegistry.registerModelLayer(ModelIndustrialAlarm.ALARM_LAYER, ModelIndustrialAlarm::createLayerDefinition);
        EntityModelLayerRegistry.registerModelLayer(ModelScubaMask.MASK_LAYER, ModelScubaMask::createLayerDefinition);
        EntityModelLayerRegistry.registerModelLayer(ModelScubaTank.TANK_LAYER, ModelScubaTank::createLayerDefinition);
        EntityModelLayerRegistry.registerModelLayer(ModelTransporterBox.BOX_LAYER, ModelTransporterBox::createLayerDefinition);
    }

    public static void registerClientReloadListeners() {
        ClientRegistrationUtil.registerClientReloadListeners(
              //ISTERs
              RenderEnergyCubeItem.RENDERER, RenderJetpack.ARMORED_RENDERER, RenderAtomicDisassembler.RENDERER, RenderFlameThrower.RENDERER, RenderFreeRunners.RENDERER,
              RenderFreeRunners.ARMORED_RENDERER, RenderJetpack.RENDERER, RenderScubaMask.RENDERER, RenderScubaTank.RENDERER,
              //Custom Armor
              JetpackArmor.ARMORED_JETPACK, JetpackArmor.JETPACK, FreeRunnerArmor.ARMORED_FREE_RUNNERS, FreeRunnerArmor.FREE_RUNNERS, ScubaMaskArmor.SCUBA_MASK,
              ScubaTankArmor.SCUBA_TANK
        );
    }

    public static void registerContainers() {
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.MODULE_TWEAKER, GuiModuleTweaker::new);

        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.DICTIONARY, GuiDictionary::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.PORTABLE_TELEPORTER, GuiPortableTeleporter::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.SEISMIC_READER, GuiSeismicReader::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.QIO_FREQUENCY_SELECT_ITEM, GuiQIOItemFrequencySelect::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.PORTABLE_QIO_DASHBOARD, GuiPortableQIODashboard::new);

        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.MAIN_ROBIT, GuiRobitMain::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.INVENTORY_ROBIT, GuiRobitInventory::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.SMELTING_ROBIT, GuiRobitSmelting::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.CRAFTING_ROBIT, GuiRobitCrafting::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.REPAIR_ROBIT, GuiRobitRepair::new);

        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.CHEMICAL_CRYSTALLIZER, GuiChemicalCrystallizer::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.CHEMICAL_DISSOLUTION_CHAMBER, GuiChemicalDissolutionChamber::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.CHEMICAL_INFUSER, GuiChemicalInfuser::new);
        ClientRegistrationUtil.registerAdvancedElectricScreen(MekanismContainerTypes.CHEMICAL_INJECTION_CHAMBER);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.CHEMICAL_OXIDIZER, GuiChemicalOxidizer::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.CHEMICAL_WASHER, GuiChemicalWasher::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.COMBINER, GuiCombiner::new);
        ClientRegistrationUtil.registerElectricScreen(MekanismContainerTypes.CRUSHER);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.DIGITAL_MINER, GuiDigitalMiner::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.DYNAMIC_TANK, GuiDynamicTank::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.ELECTRIC_PUMP, GuiElectricPump::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.ELECTROLYTIC_SEPARATOR, GuiElectrolyticSeparator::new);
        ClientRegistrationUtil.registerElectricScreen(MekanismContainerTypes.ENERGIZED_SMELTER);
        ClientRegistrationUtil.registerElectricScreen(MekanismContainerTypes.ENRICHMENT_CHAMBER);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.FLUIDIC_PLENISHER, GuiFluidicPlenisher::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.FORMULAIC_ASSEMBLICATOR, GuiFormulaicAssemblicator::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.FUELWOOD_HEATER, GuiFuelwoodHeater::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.LASER_AMPLIFIER, GuiLaserAmplifier::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.LASER_TRACTOR_BEAM, GuiLaserTractorBeam::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.METALLURGIC_INFUSER, GuiMetallurgicInfuser::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.OREDICTIONIFICATOR, GuiOredictionificator::new);
        ClientRegistrationUtil.registerAdvancedElectricScreen(MekanismContainerTypes.OSMIUM_COMPRESSOR);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.PRECISION_SAWMILL, GuiPrecisionSawmill::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.PRESSURIZED_REACTION_CHAMBER, GuiPRC::new);
        ClientRegistrationUtil.registerAdvancedElectricScreen(MekanismContainerTypes.PURIFICATION_CHAMBER);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.QUANTUM_ENTANGLOPORTER, GuiQuantumEntangloporter::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.RESISTIVE_HEATER, GuiResistiveHeater::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.ROTARY_CONDENSENTRATOR, GuiRotaryCondensentrator::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.SECURITY_DESK, GuiSecurityDesk::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.MODIFICATION_STATION, GuiModificationStation::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.ISOTOPIC_CENTRIFUGE, GuiIsotopicCentrifuge::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.NUTRITIONAL_LIQUIFIER, GuiNutritionalLiquifier::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.ANTIPROTONIC_NUCLEOSYNTHESIZER, GuiAntiprotonicNucleosynthesizer::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.PIGMENT_EXTRACTOR, GuiPigmentExtractor::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.PIGMENT_MIXER, GuiPigmentMixer::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.PAINTING_MACHINE, GuiPaintingMachine::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.SEISMIC_VIBRATOR, GuiSeismicVibrator::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.SOLAR_NEUTRON_ACTIVATOR, GuiSolarNeutronActivator::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.TELEPORTER, GuiTeleporter::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.THERMAL_EVAPORATION_CONTROLLER, GuiThermalEvaporationController::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.QIO_DRIVE_ARRAY, GuiQIODriveArray::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.QIO_DASHBOARD, GuiQIODashboard::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.QIO_IMPORTER, GuiQIOImporter::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.QIO_EXPORTER, GuiQIOExporter::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.QIO_REDSTONE_ADAPTER, GuiQIORedstoneAdapter::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.SPS, GuiSPS::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.DIMENSIONAL_STABILIZER, GuiDimensionalStabilizer::new);

        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.FACTORY, GuiFactory::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.CHEMICAL_TANK, GuiChemicalTank::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.FLUID_TANK, GuiFluidTank::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.ENERGY_CUBE, GuiEnergyCube::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.INDUCTION_MATRIX, GuiInductionMatrix::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.THERMOELECTRIC_BOILER, GuiThermoelectricBoiler::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.PERSONAL_STORAGE_ITEM, GuiPersonalStorageItem::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.PERSONAL_STORAGE_BLOCK, GuiPersonalStorageTile::new);

        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.DIGITAL_MINER_CONFIG, GuiDigitalMinerConfig::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.LOGISTICAL_SORTER, GuiLogisticalSorter::new);

        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.QIO_FREQUENCY_SELECT_TILE, GuiQIOTileFrequencySelect::new);

        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.BOILER_STATS, GuiBoilerStats::new);
        ClientRegistrationUtil.registerScreen(MekanismContainerTypes.MATRIX_STATS, GuiMatrixStats::new);
    }

    public static void registerModels() {
        RegisterGeometryLoadersCallback.EVENT.register(loaders -> {
            loaders.put(Mekanism.rl("transmitter"), TransmitterLoader.INSTANCE);
            loaders.put(new ResourceLocation("forge", "obj"), mekanism.client.render.obj.ObjModelLoader.INSTANCE);
        });
        ModelLoadingPlugin.register(context -> {
            MekanismModelCache.INSTANCE.setup(context);
            context.modifyModelAfterBake().register(ModelModifier.WRAP_PHASE, (model, bake) -> {
                ResourceLocation id = bake.id();
                CustomModelRegistryObject custom = customModels.get(new ResourceLocation(id.getNamespace(), id.getPath()));
                return model == null || custom == null ? model : custom.createModel(model, bake);
            });
        });
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return Mekanism.rl("model_cache");
            }

            @Override
            public Collection<ResourceLocation> getFabricDependencies() {
                return List.of(ResourceReloadListenerKeys.MODELS);
            }

            @Override
            public void onResourceManagerReload(ResourceManager resources) {
                ModelManager manager = Minecraft.getInstance().getModelManager();
                MekanismModelCache.INSTANCE.onBake(manager);
                MekanismRenderer.onStitch(manager.getAtlas(TextureAtlas.LOCATION_BLOCKS));
            }
        });
    }

    public static void registerParticleFactories() {
        ParticleFactoryRegistry.getInstance().register(MekanismParticleTypes.LASER.get(), LaserParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(MekanismParticleTypes.JETPACK_FLAME.get(), JetpackFlameParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(MekanismParticleTypes.JETPACK_SMOKE.get(), JetpackSmokeParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(MekanismParticleTypes.SCUBA_BUBBLE.get(), ScubaBubbleParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(MekanismParticleTypes.RADIATION.get(), RadiationParticle.Factory::new);
    }

    public static void registerBlockColorHandlers() {
        ClientRegistrationUtil.registerBlockColorHandler((state, world, pos, tintIndex) -> {
                  if (tintIndex == 1) {
                      BaseTier tier = Attribute.getBaseTier(state.getBlock());
                      if (tier != null) {
                          return MekanismRenderer.getColorARGB(tier, 1);
                      }
                  }
                  return -1;
              }, MekanismBlocks.BASIC_FLUID_TANK, MekanismBlocks.ADVANCED_FLUID_TANK, MekanismBlocks.ELITE_FLUID_TANK, MekanismBlocks.ULTIMATE_FLUID_TANK,
              MekanismBlocks.CREATIVE_FLUID_TANK);
        ClientRegistrationUtil.registerBlockColorHandler((state, world, pos, tintIndex) -> {
                  if (pos != null) {
                      TileEntityQIOComponent tile = WorldUtils.getTileEntity(TileEntityQIOComponent.class, world, pos);
                      if (tile != null) {
                          EnumColor color = tile.getColor();
                          return color == null ? -1 : MekanismRenderer.getColorARGB(color, 1);
                      }
                  }
                  return -1;
              }, MekanismBlocks.QIO_DRIVE_ARRAY, MekanismBlocks.QIO_DASHBOARD, MekanismBlocks.QIO_IMPORTER, MekanismBlocks.QIO_EXPORTER,
              MekanismBlocks.QIO_REDSTONE_ADAPTER);
        ClientRegistrationUtil.registerBlockColorHandler((state, world, pos, tintIndex) -> {
                  if (tintIndex == 1 && pos != null) {
                      TileEntityLogisticalTransporter transporter = WorldUtils.getTileEntity(TileEntityLogisticalTransporter.class, world, pos);
                      if (transporter != null) {
                          EnumColor renderColor = transporter.getTransmitter().getColor();
                          if (renderColor != null) {
                              return MekanismRenderer.getColorARGB(renderColor, 1);
                          }
                      }
                  }
                  return -1;
              }, MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER, MekanismBlocks.ADVANCED_LOGISTICAL_TRANSPORTER, MekanismBlocks.ELITE_LOGISTICAL_TRANSPORTER,
              MekanismBlocks.ULTIMATE_LOGISTICAL_TRANSPORTER);
        for (Map.Entry<IResource, BlockRegistryObject<?, ?>> entry : MekanismBlocks.PROCESSED_RESOURCE_BLOCKS.entrySet()) {
            if (entry.getKey() instanceof PrimaryResource primaryResource) {
                int tint = primaryResource.getTint();
                ClientRegistrationUtil.registerBlockColorHandler((state, world, pos, index) -> index == 1 ? tint : -1, entry.getValue());
            }
        }
    }

    public static void registerItemColorHandlers() {
        ClientRegistrationUtil.registerItemColorHandler((stack, tintIndex) -> {
                  Item item = stack.getItem();
                  if (tintIndex == 1 && item instanceof ItemBlockFluidTank tank) {
                      return MekanismRenderer.getColorARGB(tank.getTier().getBaseTier(), 1);
                  }
                  return -1;
              }, MekanismBlocks.BASIC_FLUID_TANK, MekanismBlocks.ADVANCED_FLUID_TANK, MekanismBlocks.ELITE_FLUID_TANK, MekanismBlocks.ULTIMATE_FLUID_TANK,
              MekanismBlocks.CREATIVE_FLUID_TANK);
        for (Cell<ResourceType, PrimaryResource, ItemRegistryObject<Item>> item : MekanismItems.PROCESSED_RESOURCES.cellSet()) {
            int tint = item.getColumnKey().getTint();
            ClientRegistrationUtil.registerItemColorHandler((stack, index) -> index == 1 ? tint : -1, item.getValue());
        }
        ClientRegistrationUtil.registerIColoredItemHandler(MekanismItems.PORTABLE_QIO_DASHBOARD, MekanismBlocks.QIO_DRIVE_ARRAY, MekanismBlocks.QIO_DASHBOARD,
              MekanismBlocks.QIO_IMPORTER, MekanismBlocks.QIO_EXPORTER, MekanismBlocks.QIO_REDSTONE_ADAPTER);

        ClientRegistrationUtil.registerItemColorHandler((stack, index) -> {
            if (index == 1) {
                IModule<ModuleColorModulationUnit> colorModulation = IModuleHelper.INSTANCE.load(stack, MekanismModules.COLOR_MODULATION_UNIT);
                if (colorModulation != null) {
                    Color color = colorModulation.getCustomInstance().getColor();
                    //Calculate actual tint from alpha in the same way we do in the shader. Ideally we would expose this somehow for resource packs
                    // like is done for the shader but oh well
                    color = Color.rgbd(color.ad() * color.rd() + (1.0 - color.ad()), color.ad() * color.gd() + (1.0 - color.ad()),
                          color.ad() * color.bd() + (1.0 - color.ad()));
                    return color.argb();
                }
            }
            return -1;
        }, MekanismItems.MEKASUIT_HELMET, MekanismItems.MEKASUIT_BODYARMOR, MekanismItems.MEKASUIT_PANTS, MekanismItems.MEKASUIT_BOOTS);

        for (Map.Entry<IResource, BlockRegistryObject<?, ?>> entry : MekanismBlocks.PROCESSED_RESOURCE_BLOCKS.entrySet()) {
            if (entry.getKey() instanceof PrimaryResource primaryResource) {
                int tint = primaryResource.getTint();
                ClientRegistrationUtil.registerItemColorHandler((stack, index) -> index == 1 ? tint : -1, entry.getValue());
            }
        }
    }

    public static void registerItemDecorations() {
        ItemDecorationRegistry.register(MekanismItems.MEKASUIT_HELMET, MekaSuitBarDecorator.INSTANCE);
        ItemDecorationRegistry.register(MekanismItems.MEKASUIT_BODYARMOR, MekaSuitBarDecorator.INSTANCE);
        TransmitterTypeDecorator.registerDecorators(MekanismBlocks.BASIC_PRESSURIZED_TUBE, MekanismBlocks.ADVANCED_PRESSURIZED_TUBE,
              MekanismBlocks.ELITE_PRESSURIZED_TUBE, MekanismBlocks.ULTIMATE_PRESSURIZED_TUBE, MekanismBlocks.BASIC_THERMODYNAMIC_CONDUCTOR,
              MekanismBlocks.ADVANCED_THERMODYNAMIC_CONDUCTOR, MekanismBlocks.ELITE_THERMODYNAMIC_CONDUCTOR, MekanismBlocks.ULTIMATE_THERMODYNAMIC_CONDUCTOR,
              MekanismBlocks.BASIC_UNIVERSAL_CABLE, MekanismBlocks.ADVANCED_UNIVERSAL_CABLE, MekanismBlocks.ELITE_UNIVERSAL_CABLE, MekanismBlocks.ULTIMATE_UNIVERSAL_CABLE);
    }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        //Add our own custom armor layer to the various player renderers
        for (String skinName : event.getSkins()) {
            addCustomLayers(EntityType.PLAYER, (PlayerRenderer) event.getSkin(skinName), event.getContext().getModelManager());
        }
        //Add our own custom armor layer to everything that has an armor layer
        //Note: This includes any modded mobs that have vanilla's BipedArmorLayer added to them
        for (Entry<EntityType<?>, EntityRenderer<?>> entry : Minecraft.getInstance().getEntityRenderDispatcher().renderers.entrySet()) {
            EntityRenderer<?> renderer = entry.getValue();
            if (renderer instanceof LivingEntityRenderer) {
                EntityType<?> entityType = entry.getKey();
                //noinspection unchecked,rawtypes
                addCustomLayers(entityType, event.getRenderer((EntityType) entityType), event.getContext().getModelManager());
            }
        }
    }

    private static <T extends LivingEntity, M extends HumanoidModel<T>> void addCustomLayers(EntityType<?> type, @Nullable LivingEntityRenderer<T, M> renderer,
          ModelManager modelManager) {
        if (renderer == null) {
            return;
        }
        HumanoidArmorLayer<T, M, ?> bipedArmorLayer = null;
        boolean hasElytra = false;
        for (RenderLayer<T, M> layerRenderer : renderer.layers) {
            //Validate against the layer render being null, as it seems like some mods do stupid things and add in null layers
            if (layerRenderer != null) {
                //Only allow an exact class match, so we don't add to modded entities that only have a modded extended armor or elytra layer
                Class<?> layerClass = layerRenderer.getClass();
                if (layerClass == HumanoidArmorLayer.class) {
                    bipedArmorLayer = (HumanoidArmorLayer<T, M, ?>) layerRenderer;
                    if (hasElytra) {
                        break;
                    }
                } else if (layerClass == ElytraLayer.class) {
                    hasElytra = true;
                    if (bipedArmorLayer != null) {
                        break;
                    }
                }
            }
        }
        if (bipedArmorLayer != null) {
            renderer.addLayer(new MekanismArmorLayer<>(renderer, bipedArmorLayer.innerModel, bipedArmorLayer.outerModel, modelManager));
            Mekanism.logger.debug("Added Mekanism Armor Layer to entity of type: {}", RegistryUtils.getName(type));
        }
        if (hasElytra) {
            renderer.addLayer(new MekanismElytraLayer<>(renderer, Minecraft.getInstance().getEntityModels()));
            Mekanism.logger.debug("Added Mekanism Elytra Layer to entity of type: {}", RegistryUtils.getName(type));
        }
    }

    public static void addCustomModel(IItemProvider provider, CustomModelRegistryObject object) {
        customModels.put(provider.getRegistryName(), object);
    }

    public static void addLitModel(IItemProvider... providers) {
        for (IItemProvider provider : providers) {
            addCustomModel(provider, (orig, evt) -> lightBakedModel(orig));
        }
    }

    private static BakedModel lightBakedModel(BakedModel orig) {
        return new LightedBakedModel(orig);
    }

    @FunctionalInterface
    public interface CustomModelRegistryObject {

        BakedModel createModel(BakedModel original, ModelModifier.AfterBake.Context event);
    }
}
