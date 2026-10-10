package mekanism.additions.common;

import java.util.concurrent.CompletableFuture;
import mekanism.additions.common.entity.baby.BabyType;
import mekanism.additions.common.world.modifier.BabyEntitySpawnBiomeModifier;
import mekanism.additions.common.world.modifier.BabyEntitySpawnStructureModifier;
import mekanism.common.registries.BaseDatapackRegistryProvider;
import mekanism.common.world.modifier.WorldgenModifiers;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;

public class AdditionsDatapackRegistryProvider extends BaseDatapackRegistryProvider {

    public AdditionsDatapackRegistryProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, MekanismAdditions.MODID);
    }

    public static void buildRegistry(RegistrySetBuilder builder) {
        builder
          .add(WorldgenModifiers.BIOME_MODIFIERS, context -> {
              for (BabyType babyType : BabyType.values()) {
                  context.register(biomeModifier(MekanismAdditions.rl(babyType.getSerializedName())), new BabyEntitySpawnBiomeModifier(babyType));
              }
          })
          .add(WorldgenModifiers.STRUCTURE_MODIFIERS, context -> {
              for (BabyType babyType : BabyType.values()) {
                  context.register(structureModifier(MekanismAdditions.rl(babyType.getSerializedName())), new BabyEntitySpawnStructureModifier(babyType));
              }
          });
    }
}
