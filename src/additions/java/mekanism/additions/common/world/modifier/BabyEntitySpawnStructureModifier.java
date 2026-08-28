package mekanism.additions.common.world.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import mekanism.additions.common.config.AdditionsConfig;
import mekanism.additions.common.config.MekanismAdditionsConfig;
import mekanism.additions.common.entity.baby.BabyType;
import mekanism.additions.common.registries.AdditionsStructureModifierSerializers;
import mekanism.common.Mekanism;
import mekanism.common.util.RegistryUtils;
import mekanism.common.world.modifier.StructureModifier;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;

public record BabyEntitySpawnStructureModifier(BabyType babyType, AdditionsConfig.SpawnConfig spawnConfig) implements StructureModifier {

    public BabyEntitySpawnStructureModifier(BabyType babyType) {
        this(babyType, MekanismAdditionsConfig.additions.getConfig(babyType));
    }

    @Override
    public void modify(Holder<Structure> structure, ModificationPhase phase, Map<MobCategory, StructureSpawnOverride> spawns) {
        if (phase == ModificationPhase.REMOVALS && spawnConfig.shouldSpawn.get()) {
            //Note: We need to run after addition in case we ran after any mods added their skeletons,
            // but we run before after everything to make it easier for another mod to remove us
            StructureSpawnOverride spawnOverrides = spawns.get(MobCategory.MONSTER);
            //Fail quick if there are no overrides for this structure, or it is blacklisted
            ResourceLocation structureName = BuiltInRegistries.STRUCTURE_TYPE.getKey(structure.value().type());
            if (spawnOverrides != null && !spawnConfig.structureBlackList.get().contains(structureName)) {
                List<MobSpawnSettings.SpawnerData> entries = new ArrayList<>(spawnOverrides.spawns().unwrap());
                for (MobSpawnSettings.SpawnerData spawner : spawnConfig.getSpawnersToAdd(spawnOverrides.spawns().unwrap())) {
                    entries.add(spawner);
                    Mekanism.logger.debug("Adding spawn rate for '{}' in structure '{}', with weight: {}, minSize: {}, maxSize: {}",
                          RegistryUtils.getName(spawner.type), structureName, spawner.getWeight(), spawner.minCount, spawner.maxCount);
                }
                spawns.put(MobCategory.MONSTER, new StructureSpawnOverride(spawnOverrides.boundingBox(), WeightedRandomList.create(entries)));
            }
        }
    }

    @Override
    public Codec<? extends StructureModifier> codec() {
        return AdditionsStructureModifierSerializers.SPAWN_BABIES.get();
    }

    public static Codec<BabyEntitySpawnStructureModifier> makeCodec() {
        return RecordCodecBuilder.create(builder -> builder.group(
              BabyType.CODEC.fieldOf("babyType").forGetter(BabyEntitySpawnStructureModifier::babyType)
        ).apply(builder, BabyEntitySpawnStructureModifier::new));
    }
}
