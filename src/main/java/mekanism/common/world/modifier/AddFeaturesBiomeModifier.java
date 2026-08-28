package mekanism.common.world.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.biome.v1.BiomeModificationContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public record AddFeaturesBiomeModifier(HolderSet<Biome> biomes, HolderSet<PlacedFeature> features, GenerationStep.Decoration step) implements BiomeModifier {

    public static final Codec<AddFeaturesBiomeModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          Biome.LIST_CODEC.fieldOf("biomes").forGetter(AddFeaturesBiomeModifier::biomes),
          RegistryCodecs.homogeneousList(Registries.PLACED_FEATURE).fieldOf("features").forGetter(AddFeaturesBiomeModifier::features),
          GenerationStep.Decoration.CODEC.fieldOf("step").forGetter(AddFeaturesBiomeModifier::step)
    ).apply(instance, AddFeaturesBiomeModifier::new));

    @Override
    public void modify(BiomeSelectionContext biome, ModificationPhase phase, BiomeModificationContext context) {
        if (phase == ModificationPhase.ADDITIONS && biomes.contains(biome.getBiomeRegistryEntry())) {
            features.forEach(feature -> context.getGenerationSettings().addFeature(step, feature.unwrapKey().orElseThrow()));
        }
    }

    @Override
    public Codec<AddFeaturesBiomeModifier> codec() {
        return CODEC;
    }
}
