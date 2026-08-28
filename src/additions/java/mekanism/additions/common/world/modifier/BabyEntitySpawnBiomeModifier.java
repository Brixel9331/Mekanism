package mekanism.additions.common.world.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import mekanism.additions.common.config.AdditionsConfig;
import mekanism.additions.common.config.MekanismAdditionsConfig;
import mekanism.additions.common.entity.baby.BabyType;
import mekanism.additions.common.registries.AdditionsBiomeModifierSerializers;
import mekanism.common.Mekanism;
import mekanism.common.util.RegistryUtils;
import mekanism.common.world.modifier.BiomeModifier;
import net.fabricmc.fabric.api.biome.v1.BiomeModificationContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;

public record BabyEntitySpawnBiomeModifier(BabyType babyType, AdditionsConfig.SpawnConfig spawnConfig) implements BiomeModifier {

    public BabyEntitySpawnBiomeModifier(BabyType babyType) {
        this(babyType, MekanismAdditionsConfig.additions.getConfig(babyType));
    }

    @Override
    public void modify(BiomeSelectionContext biome, ModificationPhase phase, BiomeModificationContext builder) {
        if (phase == ModificationPhase.REMOVALS && spawnConfig.shouldSpawn.get()) {
            //Note: We need to run after addition in case we ran after any mods added their skeletons,
            // but we run before after everything to make it easier for another mod to remove us
            ResourceLocation biomeName = biome.getBiomeKey().location();
            if (!spawnConfig.biomeBlackList.get().contains(biomeName)) {
                EntityType<?> parent = spawnConfig.parentTypeProvider.getEntityType();
                BiomeModificationContext.SpawnSettingsContext mobSpawnSettings = builder.getSpawnSettings();
                List<MobSpawnSettings.SpawnerData> monsterSpawns = biome.getBiome().getMobSettings().getMobs(MobCategory.MONSTER).unwrap();
                for (MobSpawnSettings.SpawnerData spawner : spawnConfig.getSpawnersToAdd(monsterSpawns)) {
                    mobSpawnSettings.addSpawn(MobCategory.MONSTER, spawner);
                    MobSpawnSettings.MobSpawnCost parentCost = biome.getBiome().getMobSettings().getMobSpawnCost(parent);
                    if (parentCost == null) {
                        Mekanism.logger.debug("Adding spawn rate for '{}' in biome '{}', with weight: {}, minSize: {}, maxSize: {}",
                              RegistryUtils.getName(spawner.type), biomeName, spawner.getWeight(), spawner.minCount, spawner.maxCount);
                    } else {
                        double spawnCostPerEntity = parentCost.charge() * spawnConfig.spawnCostPerEntityPercentage.get();
                        double maxSpawnCost = parentCost.energyBudget() * spawnConfig.maxSpawnCostPercentage.get();
                        mobSpawnSettings.setSpawnCost(spawner.type, spawnCostPerEntity, maxSpawnCost);
                        Mekanism.logger.debug("Adding spawn rate for '{}' in biome '{}', with weight: {}, minSize: {}, maxSize: {}, spawnCostPerEntity: {}, maxSpawnCost: {}",
                              RegistryUtils.getName(spawner.type), biomeName, spawner.getWeight(), spawner.minCount, spawner.maxCount, spawnCostPerEntity, maxSpawnCost);
                    }
                }
            }
        }
    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return AdditionsBiomeModifierSerializers.SPAWN_BABIES.get();
    }

    public static Codec<BabyEntitySpawnBiomeModifier> makeCodec() {
        return RecordCodecBuilder.create(builder -> builder.group(
              BabyType.CODEC.fieldOf("babyType").forGetter(BabyEntitySpawnBiomeModifier::babyType)
        ).apply(builder, BabyEntitySpawnBiomeModifier::new));
    }
}
