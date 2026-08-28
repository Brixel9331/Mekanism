package mekanism.common.world.modifier;

import com.mojang.serialization.Codec;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import mekanism.api.MekanismAPI;
import mekanism.common.mixin.BiomeSelectionContextAccessor;
import mekanism.common.mixin.StructureSettingsAccessor;
import net.fabricmc.fabric.api.biome.v1.BiomeModificationContext;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;

public final class WorldgenModifiers {

    public static final ResourceKey<Registry<BiomeModifier>> BIOME_MODIFIERS = key("biome_modifier");
    public static final ResourceKey<Registry<StructureModifier>> STRUCTURE_MODIFIERS = key("structure_modifier");
    public static final ResourceKey<Registry<Codec<? extends BiomeModifier>>> BIOME_SERIALIZERS = key("biome_modifier_serializer");
    public static final ResourceKey<Registry<Codec<? extends StructureModifier>>> STRUCTURE_SERIALIZERS = key("structure_modifier_serializer");
    public static final Codec<BiomeModifier> BIOME_CODEC = ExtraCodecs.lazyInitializedCodec(() -> biomeSerializers().byNameCodec())
          .dispatch(BiomeModifier::codec, Function.identity());
    public static final Codec<StructureModifier> STRUCTURE_CODEC = ExtraCodecs.lazyInitializedCodec(() -> structureSerializers().byNameCodec())
          .dispatch(StructureModifier::codec, Function.identity());
    private static Registry<Codec<? extends BiomeModifier>> biomeSerializers;
    private static Registry<Codec<? extends StructureModifier>> structureSerializers;
    private static boolean registered;

    private WorldgenModifiers() {
    }

    private static <T> ResourceKey<Registry<T>> key(String path) {
        return ResourceKey.createRegistryKey(new ResourceLocation(MekanismAPI.MEKANISM_MODID, path));
    }

    public static Registry<Codec<? extends BiomeModifier>> biomeSerializers() {
        if (biomeSerializers == null) {
            biomeSerializers = FabricRegistryBuilder.createSimple(BIOME_SERIALIZERS).buildAndRegister();
        }
        return biomeSerializers;
    }

    public static Registry<Codec<? extends StructureModifier>> structureSerializers() {
        if (structureSerializers == null) {
            structureSerializers = FabricRegistryBuilder.createSimple(STRUCTURE_SERIALIZERS).buildAndRegister();
        }
        return structureSerializers;
    }

    public static void register() {
        if (registered) {
            return;
        }
        Registry.register(biomeSerializers(), new ResourceLocation(MekanismAPI.MEKANISM_MODID, "add_features"), AddFeaturesBiomeModifier.CODEC);
        Registry.register(biomeSerializers(), new ResourceLocation(MekanismAPI.MEKANISM_MODID, "none"), NoneBiomeModifier.CODEC);
        Registry.register(structureSerializers(), new ResourceLocation(MekanismAPI.MEKANISM_MODID, "none"), NoneStructureModifier.CODEC);
        DynamicRegistries.register(BIOME_MODIFIERS, BIOME_CODEC);
        DynamicRegistries.register(STRUCTURE_MODIFIERS, STRUCTURE_CODEC);
        var modifications = BiomeModifications.create(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "datapack_modifiers"));
        for (ModificationPhase phase : ModificationPhase.values()) {
            modifications.add(phase, biome -> true, (biome, context) -> {
                RegistryAccess registries = ((BiomeSelectionContextAccessor) biome).mekanism$getRegistries();
                registries.registry(BIOME_MODIFIERS).ifPresent(modifiers -> modifiers.forEach(modifier -> modifier.modify(biome, phase, context)));
            });
        }
        ServerLifecycleEvents.SERVER_STARTING.register(server -> applyStructureModifiers(server.registryAccess()));
        registered = true;
    }

    public static void applyStructureModifiers(RegistryAccess registries) {
        registries.registry(STRUCTURE_MODIFIERS).ifPresent(modifiers -> registries.registryOrThrow(Registries.STRUCTURE).holders().forEach(holder -> {
            Structure structure = holder.value();
            Map<MobCategory, StructureSpawnOverride> spawns = new EnumMap<>(MobCategory.class);
            spawns.putAll(structure.spawnOverrides());
            for (ModificationPhase phase : ModificationPhase.values()) {
                modifiers.forEach(modifier -> modifier.modify(holder, phase, spawns));
            }
            if (!spawns.equals(structure.spawnOverrides())) {
                ((StructureSettingsAccessor) structure).mekanism$setSettings(new Structure.StructureSettings(structure.biomes(), Map.copyOf(spawns),
                      structure.step(), structure.terrainAdaptation()));
            }
        }));
    }

    public enum NoneBiomeModifier implements BiomeModifier {
        INSTANCE;

        public static final Codec<NoneBiomeModifier> CODEC = Codec.unit(INSTANCE);

        @Override
        public void modify(BiomeSelectionContext biome, ModificationPhase phase,
              BiomeModificationContext context) {
        }

        @Override
        public Codec<NoneBiomeModifier> codec() {
            return CODEC;
        }
    }

    public enum NoneStructureModifier implements StructureModifier {
        INSTANCE;

        public static final Codec<NoneStructureModifier> CODEC = Codec.unit(INSTANCE);

        @Override
        public void modify(Holder<Structure> structure, ModificationPhase phase, Map<MobCategory, StructureSpawnOverride> spawns) {
        }

        @Override
        public Codec<NoneStructureModifier> codec() {
            return CODEC;
        }
    }
}
