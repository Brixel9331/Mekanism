package mekanism.additions.common;

import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import mekanism.additions.client.AdditionsBlockStateProvider;
import mekanism.additions.client.AdditionsItemModelProvider;
import mekanism.additions.client.AdditionsLangProvider;
import mekanism.additions.client.AdditionsSoundProvider;
import mekanism.additions.client.AdditionsSpriteSourceProvider;
import mekanism.additions.common.loot.AdditionsLootProvider;
import mekanism.additions.common.recipe.AdditionsRecipeProvider;
import mekanism.common.BasePackMetadataGenerator;
import mekanism.common.MekanismDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class AdditionsDataGenerator implements DataGeneratorEntrypoint {

    public AdditionsDataGenerator() {
        MekanismDataGenerator.bootstrapConfigs(MekanismAdditions.MODID);
    }

    @Override
    public String getEffectiveModId() {
        return MekanismAdditions.MODID;
    }

    @Override
    public void buildRegistry(net.minecraft.core.RegistrySetBuilder builder) {
        AdditionsDatapackRegistryProvider.buildRegistry(builder);
    }

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();
        ExistingFileHelper existingFileHelper = MekanismDataGenerator.createFileHelper();
        pack.addProvider((output, registries) -> new AdditionsDatapackRegistryProvider(output, registries));
        pack.addProvider((output, registries) -> new BasePackMetadataGenerator(output, AdditionsLang.PACK_DESCRIPTION));
        //Client side data generators
        pack.addProvider((output, registries) -> new AdditionsLangProvider(output));
        pack.addProvider((output, registries) -> new AdditionsSoundProvider(output, existingFileHelper));
        pack.addProvider((output, registries) -> new AdditionsSpriteSourceProvider(output, existingFileHelper));
        pack.addProvider((output, registries) -> new AdditionsItemModelProvider(output, existingFileHelper));
        pack.addProvider((output, registries) -> new AdditionsBlockStateProvider(output, existingFileHelper));
        //Server side data generators
        pack.addProvider((output, registries) -> new AdditionsTagProvider(output, registries));
        pack.addProvider((output, registries) -> new AdditionsLootProvider(output));
        pack.addProvider((output, registries) -> new AdditionsRecipeProvider(output, existingFileHelper));
        pack.addProvider((output, registries) -> new AdditionsAdvancementProvider(output, existingFileHelper));
    }
}