package mekanism.common;

import mekanism.common.entity.EntityRobit;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import mekanism.api.Coord4D;
import mekanism.api.MekanismAPI;
import mekanism.api.MekanismIMC;
import mekanism.api.providers.IItemProvider;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.base.IModModule;
import mekanism.common.base.KeySync;
import mekanism.common.base.MekFakePlayer;
import mekanism.common.base.PlayerState;
import mekanism.common.base.TagCache;
import mekanism.common.capabilities.security.MekanismSecurity;
import mekanism.common.command.CommandMek;
import mekanism.common.command.builders.BuildCommand;
import mekanism.common.command.builders.Builders.BoilerBuilder;
import mekanism.common.command.builders.Builders.EvaporationBuilder;
import mekanism.common.command.builders.Builders.MatrixBuilder;
import mekanism.common.command.builders.Builders.SPSBuilder;
import mekanism.common.command.builders.Builders.TankBuilder;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.boiler.BoilerMultiblockData;
import mekanism.common.content.boiler.BoilerValidator;
import mekanism.common.content.evaporation.EvaporationMultiblockData;
import mekanism.common.content.evaporation.EvaporationValidator;
import mekanism.common.content.gear.MekaSuitDispenseBehavior;
import mekanism.common.content.gear.ModuleDispenseBehavior;
import mekanism.common.content.gear.ModuleHelper;
import mekanism.common.content.matrix.MatrixMultiblockData;
import mekanism.common.content.matrix.MatrixValidator;
import mekanism.common.content.network.BoxedChemicalNetwork.ChemicalTransferEvent;
import mekanism.common.content.network.EnergyNetwork.EnergyTransferEvent;
import mekanism.common.content.network.FluidNetwork.FluidTransferEvent;
import mekanism.common.content.qio.QIOGlobalItemLookup;
import mekanism.common.content.sps.SPSCache;
import mekanism.common.content.sps.SPSMultiblockData;
import mekanism.common.content.sps.SPSValidator;
import mekanism.common.content.tank.TankCache;
import mekanism.common.content.tank.TankMultiblockData;
import mekanism.common.content.tank.TankValidator;
import mekanism.common.content.transporter.PathfinderCache;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.item.block.machine.ItemBlockFluidTank.BasicCauldronInteraction;
import mekanism.common.item.block.machine.ItemBlockFluidTank.BasicDrainCauldronInteraction;
import mekanism.common.item.block.machine.ItemBlockFluidTank.FluidTankItemDispenseBehavior;
import mekanism.common.item.loot.MekanismLootFunctions;
import mekanism.common.item.predicate.CustomItemPredicate;
import mekanism.common.item.predicate.FullCanteenItemPredicate;
import mekanism.common.item.predicate.MaxedModuleContainerItemPredicate;
import mekanism.common.lib.BlockEntityLoadQueue;
import mekanism.common.lib.LivingEntityTickCallback;
import mekanism.common.lib.MekAnnotationScanner;
import mekanism.common.lib.ServerLifecycle;
import mekanism.common.lib.Version;
import mekanism.common.lib.WorldLifecycle;
import mekanism.common.lib.chunkloading.MekanismChunkManager;
import mekanism.common.lib.frequency.FrequencyManager;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.inventory.personalstorage.PersonalStorageManager;
import mekanism.common.lib.multiblock.MultiblockCache;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.lib.transmitter.DynamicBufferedNetwork.TransferEvent;
import mekanism.common.lib.transmitter.TransmitterNetworkRegistry;
import mekanism.common.network.PacketHandler;
import mekanism.common.network.to_client.PacketTransmitterUpdate;
import mekanism.common.recipe.IncompleteRecipeScanner;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.RecipeLifecycle;
import mekanism.common.recipe.condition.MekanismResourceConditions;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.registries.MekanismCreativeTabs;
import mekanism.common.registries.MekanismDataSerializers;
import mekanism.common.registries.MekanismEntityTypes;
import mekanism.common.registries.MekanismFeatures;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismGameEvents;
import mekanism.common.registries.MekanismGases;
import mekanism.common.registries.MekanismHeightProviderTypes;
import mekanism.common.registries.MekanismInfuseTypes;
import mekanism.common.registries.MekanismIntProviderTypes;
import mekanism.common.registries.MekanismItems;
import mekanism.common.registries.MekanismModules;
import mekanism.common.registries.MekanismParticleTypes;
import mekanism.common.registries.MekanismPigments;
import mekanism.common.registries.MekanismPlacementModifiers;
import mekanism.common.registries.MekanismRecipeSerializers;
import mekanism.common.registries.MekanismRobitSkins;
import mekanism.common.registries.MekanismSlurries;
import mekanism.common.registries.MekanismSounds;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tile.component.TileComponentChunkLoader.ChunkValidationCallback;
import mekanism.common.tile.machine.TileEntityOredictionificator.ODConfigValueInvalidationListener;
import mekanism.common.world.GenHandler;
import mekanism.common.world.RetrogenHandler;
import mekanism.common.world.modifier.WorldgenModifiers;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import org.slf4j.Logger;

public class Mekanism implements ModInitializer {

    public static final String MODID = MekanismAPI.MEKANISM_MODID;
    public static final String MOD_NAME = "Mekanism";
    public static final String LOG_TAG = '[' + MOD_NAME + ']';
    public static final PlayerState playerState = new PlayerState();
    /**
     * Mekanism Packet Pipeline
     */
    private final PacketHandler packetHandler;
    /**
     * Mekanism logger instance
     */
    public static final Logger logger = LogUtils.getLogger();

    /**
     * Mekanism mod instance
     */
    public static Mekanism instance;
    /**
     * Mekanism hooks instance
     */
    public static final MekanismHooks hooks = new MekanismHooks();
    /**
     * Mekanism version number
     */
    public final Version versionNumber;
    /**
     * MultiblockManagers for various structures
     */
    public static final MultiblockManager<TankMultiblockData> tankManager = new MultiblockManager<>("dynamicTank", TankCache::new, TankValidator::new);
    public static final MultiblockManager<MatrixMultiblockData> matrixManager = new MultiblockManager<>("inductionMatrix", MultiblockCache::new, MatrixValidator::new);
    public static final MultiblockManager<BoilerMultiblockData> boilerManager = new MultiblockManager<>("thermoelectricBoiler", MultiblockCache::new, BoilerValidator::new);
    public static final MultiblockManager<EvaporationMultiblockData> evaporationManager = new MultiblockManager<>("evaporation", MultiblockCache::new, EvaporationValidator::new);
    public static final MultiblockManager<SPSMultiblockData> spsManager = new MultiblockManager<>("sps", SPSCache::new, SPSValidator::new);
    /**
     * List of Mekanism modules loaded
     */
    public static final List<IModModule> modulesLoaded = new ArrayList<>();
    /**
     * The server's world tick handler.
     */
    public static final CommonWorldTickHandler worldTickHandler = new CommonWorldTickHandler();
    /**
     * The GameProfile used by the dummy Mekanism player
     */
    public static final GameProfile gameProfile = new GameProfile(UUID.nameUUIDFromBytes("mekanism.common".getBytes(StandardCharsets.UTF_8)), Mekanism.LOG_TAG);
    public static final KeySync keyMap = new KeySync();
    public static final Set<Coord4D> activeVibrators = new ObjectOpenHashSet<>();

    public static final RetrogenHandler retrogenHandler = new RetrogenHandler(() -> MekanismConfig.world.userGenVersion.get(),
          () -> MekanismConfig.world.enableRegeneration.get(), GenHandler::generate);

    private final ReloadListener recipeCacheManager = new ReloadListener();
    private final CommonPlayerTickHandler playerTickHandler = new CommonPlayerTickHandler();
    private boolean initialized;

    public Mekanism() {
        instance = this;
        mekanism.common.registries.MekanismAttributes.register();
        mekanism.common.lib.CraftingResultCallback.EVENT.register(mekanism.common.recipe.bin.BinInsertRecipe::onCrafting);
        MekanismSecurity.register();
        MekanismConfig.registerConfigs();
        //Register our custom serializer condition
        MekanismResourceConditions.register();

        TransferEvent.EVENT.register(event -> {
            if (event instanceof EnergyTransferEvent energy) {
                onEnergyTransferred(energy);
            } else if (event instanceof ChemicalTransferEvent chemical) {
                onChemicalTransferred(chemical);
            } else if (event instanceof FluidTransferEvent fluid) {
                onLiquidTransferred(fluid);
            }
        });
        WorldLifecycle.register();
        BlockEntityLoadQueue.register();
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            if (entity instanceof EntityRobit robit) {
                robit.onRemovedFromWorld();
            }
        });
        WorldLifecycle.LOAD.register(this::onWorldLoad);
        WorldLifecycle.UNLOAD.register(this::onWorldUnload);
        CommandRegistrationCallback.EVENT.register((dispatcher, registries, selection) -> registerCommands(dispatcher));
        ServerLifecycle.register();
        ServerLifecycleEvents.SERVER_STOPPED.register(this::serverStopped);
        worldTickHandler.register();
        playerTickHandler.register();
        LivingEntityTickCallback.EVENT.register(RadiationManager.get()::onLivingTick);
        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> TagCache.resetTagCaches());
        RecipeLifecycle.register();
        recipeCacheManager.register();
        IncompleteRecipeScanner.register();
        MekanismItems.ITEMS.register();
        MekanismBlocks.BLOCKS.register();
        MekanismFluids.FLUIDS.register();
        MekanismContainerTypes.CONTAINER_TYPES.register();
        MekanismCreativeTabs.CREATIVE_TABS.register();
        MekanismDataSerializers.DATA_SERIALIZERS.register();
        MekanismEntityTypes.ENTITY_TYPES.register();
        MekanismTileEntityTypes.TILE_ENTITY_TYPES.register();
        MekanismGameEvents.GAME_EVENTS.register();
        MekanismSounds.SOUND_EVENTS.register();
        MekanismParticleTypes.PARTICLE_TYPES.register();
        MekanismHeightProviderTypes.HEIGHT_PROVIDER_TYPES.register();
        MekanismIntProviderTypes.INT_PROVIDER_TYPES.register();
        MekanismPlacementModifiers.PLACEMENT_MODIFIERS.register();
        MekanismFeatures.FEATURES.register();
        WorldgenModifiers.register();
        retrogenHandler.register();
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> GenHandler.reset());
        MekanismRecipeType.RECIPE_TYPES.register();
        MekanismRecipeSerializers.RECIPE_SERIALIZERS.register();
        MekanismLootFunctions.REGISTER.register();
        MekanismGases.GASES.register();
        MekanismInfuseTypes.INFUSE_TYPES.register();
        MekanismPigments.PIGMENTS.register();
        MekanismSlurries.SLURRIES.register();
        MekanismRobitSkins.createAndRegisterDatapack();
        MekanismModules.MODULES.register();
        //Set our version number to match the mods.toml file, which matches the one in our build.gradle
        versionNumber = new Version(FabricLoader.getInstance().getModContainer(MODID).orElseThrow());
        packetHandler = new PacketHandler();
        //Super early hooks, only reliable thing is for checking dependencies that we declare we are after
        hooks.hookConstructor();
    }

    @Override
    public synchronized void onInitialize() {
        if (initialized) {
            return;
        }
        imcQueue();
        commonSetup();
        imcHandle();
        initialized = true;
    }

    public static void initialize() {
        Mekanism mekanism = instance;
        if (mekanism == null) {
            mekanism = FabricLoader.getInstance().getEntrypointContainers("main", ModInitializer.class).stream()
                  .filter(container -> container.getProvider().getMetadata().getId().equals(MODID))
                  .map(container -> container.getEntrypoint())
                  .filter(Mekanism.class::isInstance)
                  .map(Mekanism.class::cast)
                  .findFirst().orElseThrow(() -> new IllegalStateException("Missing Mekanism initializer"));
        }
        mekanism.onInitialize();
    }

    public static synchronized void addModule(IModModule modModule) {
        modulesLoaded.add(modModule);
    }

    public static PacketHandler packetHandler() {
        return instance.packetHandler;
    }

    public static ResourceLocation rl(String path) {
        return new ResourceLocation(Mekanism.MODID, path);
    }

    public ReloadListener getRecipeCacheManager() {
        return recipeCacheManager;
    }

    private void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        BuildCommand.register("boiler", MekanismLang.BOILER, new BoilerBuilder());
        BuildCommand.register("matrix", MekanismLang.MATRIX, new MatrixBuilder());
        BuildCommand.register("tank", MekanismLang.DYNAMIC_TANK, new TankBuilder());
        BuildCommand.register("evaporation", MekanismLang.EVAPORATION_PLANT, new EvaporationBuilder());
        BuildCommand.register("sps", MekanismLang.SPS, new SPSBuilder());
        dispatcher.register(CommandMek.register());
    }

    private void serverStopped(MinecraftServer server) {
        //Clear all cache data, wait until server stopper though so that we make sure saving can use any data it needs
        playerState.clear(false);
        activeVibrators.clear();
        FrequencyType.clear();
        BoilerMultiblockData.hotMap.clear();

        //Reset consistent managers
        QIOGlobalItemLookup.INSTANCE.reset();
        RadiationManager.get().reset();
        MultiblockManager.reset();
        FrequencyManager.reset();
        TransporterManager.reset();
        PathfinderCache.reset();
        TransmitterNetworkRegistry.reset();
        PersonalStorageManager.reset();
    }

    private void imcQueue() {
        //IMC messages we send to other mods
        //IMC messages that we are sending to ourselves
        MekanismIMC.addModulesToAll(MekanismModules.ENERGY_UNIT);
        MekanismIMC.addMekaSuitModules(MekanismModules.COLOR_MODULATION_UNIT, MekanismModules.LASER_DISSIPATION_UNIT, MekanismModules.RADIATION_SHIELDING_UNIT);
        MekanismIMC.addMekaToolModules(MekanismModules.ATTACK_AMPLIFICATION_UNIT, MekanismModules.SILK_TOUCH_UNIT, MekanismModules.FORTUNE_UNIT, MekanismModules.BLASTING_UNIT, MekanismModules.VEIN_MINING_UNIT,
              MekanismModules.FARMING_UNIT, MekanismModules.SHEARING_UNIT, MekanismModules.TELEPORTATION_UNIT, MekanismModules.EXCAVATION_ESCALATION_UNIT);
        MekanismIMC.addMekaSuitHelmetModules(MekanismModules.ELECTROLYTIC_BREATHING_UNIT, MekanismModules.INHALATION_PURIFICATION_UNIT,
              MekanismModules.VISION_ENHANCEMENT_UNIT, MekanismModules.NUTRITIONAL_INJECTION_UNIT);
        MekanismIMC.addMekaSuitBodyarmorModules(MekanismModules.JETPACK_UNIT, MekanismModules.GRAVITATIONAL_MODULATING_UNIT, MekanismModules.CHARGE_DISTRIBUTION_UNIT,
              MekanismModules.DOSIMETER_UNIT, MekanismModules.GEIGER_UNIT, MekanismModules.ELYTRA_UNIT);
        MekanismIMC.addMekaSuitPantsModules(MekanismModules.LOCOMOTIVE_BOOSTING_UNIT, MekanismModules.GYROSCOPIC_STABILIZATION_UNIT,
              MekanismModules.HYDROSTATIC_REPULSOR_UNIT, MekanismModules.MOTORIZED_SERVO_UNIT);
        MekanismIMC.addMekaSuitBootsModules(MekanismModules.HYDRAULIC_PROPULSION_UNIT, MekanismModules.MAGNETIC_ATTRACTION_UNIT, MekanismModules.FROST_WALKER_UNIT);
    }

    private void imcHandle() {
        ModuleHelper.get().processIMC();
    }

    private void commonSetup() {
        //Initialization notification
        logger.info("Version {} initializing...", versionNumber);
        hooks.hookCommonSetup();

        //Ensure our tags are all initialized
        MekanismTags.init();
        //Collect annotation scan data
        MekAnnotationScanner.collectScanData();
        //Register advancement criteria
        MekanismCriteriaTriggers.init();
        //Add chunk loading callbacks
        MekanismChunkManager.setForcedChunkLoadingCallback(Mekanism.MODID, ChunkValidationCallback.INSTANCE);
        //Register dispenser behaviors
        MekanismFluids.FLUIDS.registerBucketDispenserBehavior();
        registerFluidTankBehaviors(MekanismBlocks.BASIC_FLUID_TANK, MekanismBlocks.ADVANCED_FLUID_TANK, MekanismBlocks.ELITE_FLUID_TANK,
              MekanismBlocks.ULTIMATE_FLUID_TANK, MekanismBlocks.CREATIVE_FLUID_TANK);
        registerDispenseBehavior(new ModuleDispenseBehavior(), MekanismItems.MEKA_TOOL);
        registerDispenseBehavior(new MekaSuitDispenseBehavior(), MekanismItems.MEKASUIT_HELMET, MekanismItems.MEKASUIT_BODYARMOR, MekanismItems.MEKASUIT_PANTS,
              MekanismItems.MEKASUIT_BOOTS);
        //Register custom item predicates
        CustomItemPredicate.register(FullCanteenItemPredicate.ID, json -> FullCanteenItemPredicate.INSTANCE);
        CustomItemPredicate.register(MaxedModuleContainerItemPredicate.ID, MaxedModuleContainerItemPredicate::fromJson);
        //Add any extra game event frequencies
        MekanismGameEvents.addFrequencies();

        //Register player tracker
        new CommonPlayerTracker();

        //Register with TransmitterNetworkRegistry
        TransmitterNetworkRegistry.initiate();

        //Packet registrations
        packetHandler.initialize();

        //Fake player info
        logger.info("Fake player readout: UUID = {}, name = {}", gameProfile.getId(), gameProfile.getName());
        logger.info("Mod loaded.");
    }

    private static void registerDispenseBehavior(DispenseItemBehavior behavior, IItemProvider... itemProviders) {
        for (IItemProvider itemProvider : itemProviders) {
            DispenserBlock.registerBehavior(itemProvider.asItem(), behavior);
        }
    }

    private static void registerFluidTankBehaviors(IItemProvider... itemProviders) {
        registerDispenseBehavior(FluidTankItemDispenseBehavior.INSTANCE);
        for (IItemProvider itemProvider : itemProviders) {
            Item item = itemProvider.asItem();
            CauldronInteraction.EMPTY.put(item, BasicCauldronInteraction.EMPTY);
            CauldronInteraction.WATER.put(item, BasicDrainCauldronInteraction.WATER);
            CauldronInteraction.LAVA.put(item, BasicDrainCauldronInteraction.LAVA);
        }
    }

    private void onEnergyTransferred(EnergyTransferEvent event) {
        packetHandler.sendToReceivers(new PacketTransmitterUpdate(event.network), event.network);
    }

    private void onChemicalTransferred(ChemicalTransferEvent event) {
        packetHandler.sendToReceivers(new PacketTransmitterUpdate(event.network, event.transferType), event.network);
    }

    private void onLiquidTransferred(FluidTransferEvent event) {
        packetHandler.sendToReceivers(new PacketTransmitterUpdate(event.network, event.fluidType), event.network);
    }

    private void onWorldLoad(Level world) {
        playerState.init(world);
    }

    private void onWorldUnload(Level world) {
        // Make sure the global fake player drops its reference to the World
        // when the server shuts down
        if (world instanceof ServerLevel level) {
            MekFakePlayer.releaseInstance(level);
        }
        if (MekanismConfig.general.validOredictionificatorFilters.hasInvalidationListeners()) {
            //Remove any invalidation listeners that loaded oredictionificators might have added if the OD was in the given level
            MekanismConfig.general.validOredictionificatorFilters.removeInvalidationListenersMatching(listener ->
                  listener instanceof ODConfigValueInvalidationListener odListener && odListener.isIn(world));
        }
    }
}
