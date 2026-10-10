package mekanism.common.registries;

import it.unimi.dsi.fastutil.booleans.Boolean2ObjectFunction;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import mekanism.common.world.modifier.BiomeModifier;
import mekanism.common.world.modifier.StructureModifier;
import mekanism.common.world.modifier.WorldgenModifiers;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import org.jetbrains.annotations.NotNull;

public abstract class BaseDatapackRegistryProvider extends FabricDynamicRegistryProvider {

    private final String modid;

    protected BaseDatapackRegistryProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries, String modid) {
        super(output, registries);
        this.modid = modid;
    }

    @Override
    protected void configure(HolderLookup.Provider registries, Entries entries) {
        DynamicRegistries.getDynamicRegistries().forEach(registry ->
              registries.lookup(registry.key()).ifPresent(entries::addAll));
    }

    @NotNull
    @Override
    public String getName() {
        return "Datapack registries: " + modid;
    }

    protected static PlacedFeaturesHolder registerPlacedFeature(BootstapContext<PlacedFeature> context, ResourceLocation name,
          Boolean2ObjectFunction<List<PlacementModifier>> placementModifiers) {
        return registerPlacedFeature(context, name, name, placementModifiers);
    }

    protected static PlacedFeaturesHolder registerPlacedFeature(BootstapContext<PlacedFeature> context, ResourceLocation name, ResourceLocation retrogenName,
          Boolean2ObjectFunction<List<PlacementModifier>> placementModifiers) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);
        Reference<ConfiguredFeature<?, ?>> configuredFeature = configuredFeatures.getOrThrow(configuredFeature(name));
        Reference<ConfiguredFeature<?, ?>> retrogenConfiguredFeature = configuredFeatures.getOrThrow(configuredFeature(retrogenName));
        return new PlacedFeaturesHolder(
              context.register(placedFeature(name), new PlacedFeature(configuredFeature, placementModifiers.get(false))),
              context.register(placedFeature(name.withSuffix("_retrogen")), new PlacedFeature(retrogenConfiguredFeature, placementModifiers.get(true)))
        );
    }

    protected static ResourceKey<ConfiguredFeature<?, ?>> configuredFeature(ResourceLocation name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, name);
    }

    protected static ResourceKey<PlacedFeature> placedFeature(ResourceLocation name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, name);
    }

    protected static ResourceKey<BiomeModifier> biomeModifier(ResourceLocation name) {
        return ResourceKey.create(WorldgenModifiers.BIOME_MODIFIERS, name);
    }

    protected static ResourceKey<StructureModifier> structureModifier(ResourceLocation name) {
        return ResourceKey.create(WorldgenModifiers.STRUCTURE_MODIFIERS, name);
    }

    protected record PlacedFeaturesHolder(Holder.Reference<PlacedFeature> feature, Holder.Reference<PlacedFeature> retrogen) {

    }
}
