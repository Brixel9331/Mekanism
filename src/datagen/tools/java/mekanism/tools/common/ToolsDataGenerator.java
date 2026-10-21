package mekanism.tools.common;

import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import mekanism.common.BasePackMetadataGenerator;
import mekanism.common.MekanismDataGenerator;
import mekanism.tools.client.ToolsItemModelProvider;
import mekanism.tools.client.ToolsLangProvider;
import mekanism.tools.client.ToolsSpriteSourceProvider;
import mekanism.tools.common.recipe.ToolsRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class ToolsDataGenerator implements DataGeneratorEntrypoint {

    public ToolsDataGenerator() {
        MekanismDataGenerator.bootstrapConfigs(MekanismTools.MODID);
    }

    @Override
    public String getEffectiveModId() {
        return MekanismTools.MODID;
    }

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();
        ExistingFileHelper existingFileHelper = MekanismDataGenerator.createFileHelper();
        pack.addProvider((output, registries) -> new BasePackMetadataGenerator(output, ToolsLang.PACK_DESCRIPTION));
        //Client side data generators
        pack.addProvider((output, registries) -> new ToolsLangProvider(output));
        pack.addProvider((output, registries) -> new ToolsSpriteSourceProvider(output, existingFileHelper));
        pack.addProvider((output, registries) -> new ToolsItemModelProvider(output, existingFileHelper));
        //Server side data generators
        pack.addProvider((output, registries) -> new ToolsTagProvider(output, registries));
        pack.addProvider((output, registries) -> new ToolsRecipeProvider(output, existingFileHelper));
        pack.addProvider((output, registries) -> new ToolsAdvancementProvider(output, existingFileHelper));
    }
}