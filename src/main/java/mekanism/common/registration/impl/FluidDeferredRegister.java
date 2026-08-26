package mekanism.common.registration.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import mekanism.api.MekanismAPI;
import mekanism.api.fluid.FluidStack;
import mekanism.common.base.IChemicalConstant;
import mekanism.common.fluid.MekanismFlowingFluid.Flowing;
import mekanism.common.fluid.MekanismFlowingFluid.Source;
import mekanism.common.lib.Lazy;
import mekanism.common.registration.DeferredRegister;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FluidDeferredRegister {

    private static final ResourceLocation OVERLAY = new ResourceLocation("block/water_overlay");
    private static final ResourceLocation RENDER_OVERLAY = new ResourceLocation("misc/underwater");
    private static final ResourceLocation LIQUID = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "liquid/liquid");
    private static final ResourceLocation LIQUID_FLOW = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "liquid/liquid_flow");
    //Copy of/based off of vanilla's lava/water bucket dispense behavior
    private static final DispenseItemBehavior BUCKET_DISPENSE_BEHAVIOR = new DefaultDispenseItemBehavior() {
        @NotNull
        @Override
        public ItemStack execute(@NotNull BlockSource source, @NotNull ItemStack stack) {
            Level world = source.getLevel();
            DispensibleContainerItem bucket = (DispensibleContainerItem) stack.getItem();
            BlockPos pos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
            if (bucket.emptyContents(null, world, pos, null)) {
                bucket.checkExtraContent(null, world, stack, pos);
                return new ItemStack(Items.BUCKET);
            }
            return super.execute(source, stack);
        }
    };

    public static FluidTypeProperties getMekBaseBuilder() {
        return new FluidTypeProperties();
    }

    private final List<FluidRegistryObject<? extends MekanismFluidType, ?, ?, ?, ?>> allFluids = new ArrayList<>();

    private final DeferredRegister<Fluid> fluidRegister;
    private final DeferredRegister<Block> blockRegister;
    private final DeferredRegister<Item> itemRegister;
    private final String modid;
    private boolean registered;

    public FluidDeferredRegister(String modid) {
        this.modid = modid;
        blockRegister = new DeferredRegister<>(modid, BuiltInRegistries.BLOCK.key(), () -> BuiltInRegistries.BLOCK);
        fluidRegister = new DeferredRegister<>(modid, BuiltInRegistries.FLUID.key(), () -> BuiltInRegistries.FLUID);
        itemRegister = new DeferredRegister<>(modid, BuiltInRegistries.ITEM.key(), () -> BuiltInRegistries.ITEM);
    }

    public FluidRegistryObject<MekanismFluidType, Source, Flowing, LiquidBlock, BucketItem> registerLiquidChemical(IChemicalConstant constants) {
        int density = Math.round(constants.getDensity());
        return register(constants.getName(), properties -> properties
              .temperature(Math.round(constants.getTemperature()))
              .density(density)
              .viscosity(density)
              .lightLevel(constants.getLightLevel()), renderProperties -> renderProperties
              .tint(constants.getColor())
        );
    }

    public FluidRegistryObject<MekanismFluidType, Source, Flowing, LiquidBlock, BucketItem> register(String name, UnaryOperator<FluidTypeRenderProperties> renderProperties) {
        return register(name, UnaryOperator.identity(), renderProperties);
    }

    public FluidRegistryObject<MekanismFluidType, Source, Flowing, LiquidBlock, BucketItem> register(String name, UnaryOperator<FluidTypeProperties> properties,
          UnaryOperator<FluidTypeRenderProperties> renderProperties) {
        return register(name, (supplier, builder) -> new BucketItem(supplier.get(), builder), properties, renderProperties);
    }

    public <BUCKET extends BucketItem> FluidRegistryObject<MekanismFluidType, Source, Flowing, LiquidBlock, BUCKET> register(String name, BucketCreator<BUCKET> bucketCreator,
          UnaryOperator<FluidTypeProperties> fluidProperties, UnaryOperator<FluidTypeRenderProperties> renderProperties) {
        return register(name, fluidProperties.apply(getMekBaseBuilder()), renderProperties.apply(FluidTypeRenderProperties.builder()), bucketCreator,
              MekanismFluidType::new);
    }

    public <TYPE extends MekanismFluidType, BUCKET extends BucketItem> FluidRegistryObject<TYPE, Source, Flowing, LiquidBlock, BUCKET> register(String name,
          FluidTypeProperties properties, FluidTypeRenderProperties renderProperties, BucketCreator<BUCKET> bucketCreator,
          BiFunction<FluidTypeProperties, FluidTypeRenderProperties, TYPE> fluidTypeCreator) {
        String flowingName = "flowing_" + name;
        String bucketName = name + "_bucket";
        //Set the translation string to the same as the block
        properties.descriptionId(Util.makeDescriptionId("block", new ResourceLocation(modid, name)));
        //Create the registry object and let the values init to null as before we actually call get on them, we will update the backing values
        FluidRegistryObject<TYPE, Source, Flowing, LiquidBlock, BUCKET> fluidRegistryObject = new FluidRegistryObject<>();
        //Update the references to objects that are retrieved from the deferred registers
        fluidRegistryObject.updateFluidType(Lazy.of(() -> fluidTypeCreator.apply(properties, renderProperties)));
        fluidRegistryObject.updateStill(fluidRegister.registerEntry(name, () -> new Source(fluidRegistryObject)));
        fluidRegistryObject.updateFlowing(fluidRegister.registerEntry(flowingName, () -> new Flowing(fluidRegistryObject)));
        //Pass in suppliers that are wrapped instead of direct references to the registry objects, so that when we update the registry object to
        // point to a new object it gets updated properly.
        fluidRegistryObject.updateBucket(itemRegister.registerEntry(bucketName, () -> bucketCreator.create(fluidRegistryObject::getStillFluid,
              new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET))));
        MapColor color = getClosestColor(renderProperties.color);
        //Note: The block properties used here is a copy of the ones for water
        fluidRegistryObject.updateBlock(blockRegister.registerEntry(name, () -> new LiquidBlock(fluidRegistryObject.getStillFluid(), BlockBehaviour.Properties.of()
              .noCollission().strength(100.0F).noLootTable().replaceable().pushReaction(PushReaction.DESTROY).liquid().mapColor(color)
              .lightLevel(state -> fluidRegistryObject.getFluidType().getLightLevel()))));
        allFluids.add(fluidRegistryObject);
        return fluidRegistryObject;
    }

    private static MapColor getClosestColor(int tint) {
        if (tint == 0xFFFFFFFF) {
            return MapColor.NONE;
        }
        int red = FastColor.ARGB32.red(tint);
        int green = FastColor.ARGB32.green(tint);
        int blue = FastColor.ARGB32.blue(tint);
        MapColor color = MapColor.NONE;
        double minDistance = Double.MAX_VALUE;
        for (int id = 0; id < 64; id++) {
            MapColor toTest = MapColor.byId(id);
            if (toTest != MapColor.NONE) {
                int testRed = FastColor.ARGB32.red(toTest.col);
                int testGreen = FastColor.ARGB32.green(toTest.col);
                int testBlue = FastColor.ARGB32.blue(toTest.col);
                double distanceSquare = perceptualColorDistanceSquared(red, green, blue, testRed, testGreen, testBlue);
                if (distanceSquare < minDistance) {
                    minDistance = distanceSquare;
                    color = toTest;
                }
            }
        }
        return color;
    }

    /**
     * <a href="http://www.compuphase.com/cmetric.htm">Color Metric</a>
     * <a href="http://stackoverflow.com/a/6334454">Stack Overflow</a>
     * Returns 0 for equal colors, nonzero for colors that look different. The return value is farther from 0 the more different the colors look.
     */
    private static double perceptualColorDistanceSquared(int red1, int green1, int blue1, int red2, int green2, int blue2) {
        int redMean = (red1 + red2) >> 1;
        int r = red1 - red2;
        int g = green1 - green2;
        int b = blue1 - blue2;
        return (((512 + redMean) * r * r) >> 8) + 4 * g * g + (((767 - redMean) * b * b) >> 8);
    }

    public void register() {
        if (registered) {
            return;
        }
        fluidRegister.register();
        blockRegister.register();
        itemRegister.register();
        for (FluidRegistryObject<? extends MekanismFluidType, ?, ?, ?, ?> entry : allFluids) {
            FluidVariantAttributes.register(entry.getStillFluid(), entry.getFluidType());
            FluidVariantAttributes.register(entry.getFlowingFluid(), entry.getFluidType());
        }
        registered = true;
    }

    public List<FluidRegistryObject<? extends MekanismFluidType, ?, ?, ?, ?>> getAllFluids() {
        return Collections.unmodifiableList(allFluids);
    }

    public void registerBucketDispenserBehavior() {
        for (FluidRegistryObject<?, ?, ?, ?, ?> fluidRO : getAllFluids()) {
            DispenserBlock.registerBehavior(fluidRO.getBucket(), BUCKET_DISPENSE_BEHAVIOR);
        }
    }

    @FunctionalInterface
    public interface BucketCreator<BUCKET extends BucketItem> {

        BUCKET create(Supplier<? extends Fluid> supplier, Properties builder);
    }

    public static class FluidTypeRenderProperties {

        private ResourceLocation stillTexture = LIQUID;
        private ResourceLocation flowingTexture = LIQUID_FLOW;
        //For now all our fluids use the same "overlay" for being against glass as vanilla water.
        private ResourceLocation overlayTexture = OVERLAY;
        private ResourceLocation renderOverlayTexture = RENDER_OVERLAY;
        private int color = 0xFFFFFFFF;

        private FluidTypeRenderProperties() {
        }

        public static FluidTypeRenderProperties builder() {
            return new FluidTypeRenderProperties();
        }

        public FluidTypeRenderProperties texture(ResourceLocation still, ResourceLocation flowing) {
            this.stillTexture = still;
            this.flowingTexture = flowing;
            return this;
        }

        public FluidTypeRenderProperties texture(ResourceLocation still, ResourceLocation flowing, ResourceLocation overlay) {
            this.stillTexture = still;
            this.flowingTexture = flowing;
            this.overlayTexture = overlay;
            return this;
        }

        public FluidTypeRenderProperties renderOverlay(ResourceLocation renderOverlay) {
            this.renderOverlayTexture = renderOverlay;
            return this;
        }

        public FluidTypeRenderProperties tint(int color) {
            this.color = color;
            return this;
        }
    }

    public static class MekanismFluidType implements FluidVariantAttributeHandler {

        public final ResourceLocation stillTexture;
        public final ResourceLocation flowingTexture;
        public final ResourceLocation overlayTexture;
        public final ResourceLocation renderOverlayTexture;
        private final int color;
        private final String descriptionId;
        private final int temperature;
        private final int density;
        private final int viscosity;
        private final int lightLevel;

        public MekanismFluidType(FluidTypeProperties properties, FluidTypeRenderProperties renderProperties) {
            this.descriptionId = properties.descriptionId;
            this.temperature = properties.temperature;
            this.density = properties.density;
            this.viscosity = properties.viscosity;
            this.lightLevel = properties.lightLevel;
            this.stillTexture = renderProperties.stillTexture;
            this.flowingTexture = renderProperties.flowingTexture;
            this.overlayTexture = renderProperties.overlayTexture;
            this.renderOverlayTexture = renderProperties.renderOverlayTexture;
            this.color = renderProperties.color;
        }

        public boolean isVaporizedOnPlacement(Level level, BlockPos pos, FluidStack stack) {
            //TODO - 1.19: Decide on this for our fluids for now default to not vaporizing
            return false;
        }

        public int getColor() {
            return color;
        }

        public int getDensity() {
            return density;
        }

        public int getLightLevel() {
            return lightLevel;
        }

        @Override
        public Component getName(FluidVariant variant) {
            return Component.translatable(descriptionId);
        }

        @Override
        public Optional<SoundEvent> getFillSound(FluidVariant variant) {
            return Optional.of(SoundEvents.BUCKET_FILL);
        }

        @Override
        public Optional<SoundEvent> getEmptySound(FluidVariant variant) {
            return Optional.of(SoundEvents.BUCKET_EMPTY);
        }

        @Override
        public int getLuminance(FluidVariant variant) {
            return lightLevel;
        }

        @Override
        public int getTemperature(FluidVariant variant) {
            return temperature;
        }

        @Override
        public int getViscosity(FluidVariant variant, @Nullable Level world) {
            return viscosity;
        }

        @Override
        public boolean isLighterThanAir(FluidVariant variant) {
            return density <= 0;
        }
    }

    public static class FluidTypeProperties {

        private String descriptionId;
        private int temperature = 300;
        private int density = 1_000;
        private int viscosity = 1_000;
        private int lightLevel;

        public FluidTypeProperties descriptionId(String descriptionId) {
            this.descriptionId = descriptionId;
            return this;
        }

        public FluidTypeProperties temperature(int temperature) {
            if (temperature < 0) {
                throw new IllegalArgumentException("Fluid temperature must be nonnegative");
            }
            this.temperature = temperature;
            return this;
        }

        public FluidTypeProperties density(int density) {
            this.density = density;
            return this;
        }

        public FluidTypeProperties viscosity(int viscosity) {
            if (viscosity <= 0) {
                throw new IllegalArgumentException("Fluid viscosity must be positive");
            }
            this.viscosity = viscosity;
            return this;
        }

        public FluidTypeProperties lightLevel(int lightLevel) {
            if (lightLevel < 0 || lightLevel > 15) {
                throw new IllegalArgumentException("Fluid light level must be between 0 and 15");
            }
            this.lightLevel = lightLevel;
            return this;
        }
    }
}
