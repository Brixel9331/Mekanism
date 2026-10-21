package mekanism.generators.common;

import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import mekanism.common.BasePackMetadataGenerator;
import mekanism.common.MekanismDataGenerator;
import mekanism.generators.client.GeneratorsBlockStateProvider;
import mekanism.generators.client.GeneratorsItemModelProvider;
import mekanism.generators.client.GeneratorsLangProvider;
import mekanism.generators.client.GeneratorsSoundProvider;
import mekanism.generators.client.GeneratorsSpriteSourceProvider;
import mekanism.generators.common.loot.GeneratorsLootProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class GeneratorsDataGenerator implements DataGeneratorEntrypoint {

    public GeneratorsDataGenerator() {
        MekanismDataGenerator.bootstrapConfigs(MekanismGenerators.MODID);
    }

    @Override
    public String getEffectiveModId() {
        return MekanismGenerators.MODID;
    }

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();
        ExistingFileHelper existingFileHelper = MekanismDataGenerator.createFileHelper();
        pack.addProvider((output, registries) -> new BasePackMetadataGenerator(output, GeneratorsLang.PACK_DESCRIPTION));
        //Client side data generators
        pack.addProvider((output, registries) -> new GeneratorsLangProvider(output));
        pack.addProvider((output, registries) -> new GeneratorsSoundProvider(output, existingFileHelper));
        pack.addProvider((output, registries) -> new GeneratorsSpriteSourceProvider(output, existingFileHelper));
        pack.addProvider((output, registries) -> new GeneratorsItemModelProvider(output, existingFileHelper));
        pack.addProvider((output, registries) -> new GeneratorsBlockStateProvider(output, existingFileHelper));
        //Server side data generators
        pack.addProvider((output, registries) -> new GeneratorsTagProvider(output, registries));
        pack.addProvider((output, registries) -> new GeneratorsLootProvider(output));
        pack.addProvider((output, registries) -> new GeneratorsRecipeProvider(output, existingFileHelper));
        pack.addProvider((output, registries) -> new GeneratorsAdvancementProvider(output, existingFileHelper));
    }
}