package mekanism.defense.common;

import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import mekanism.common.BasePackMetadataGenerator;
import mekanism.common.MekanismDataGenerator;
import mekanism.defense.client.DefenseLangProvider;
import mekanism.defense.common.loot.DefenseLootProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class DefenseDataGenerator implements DataGeneratorEntrypoint {

    public DefenseDataGenerator() {
        MekanismDataGenerator.bootstrapConfigs(MekanismDefense.MODID);
    }

    @Override
    public String getEffectiveModId() {
        return MekanismDefense.MODID;
    }

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();
        ExistingFileHelper existingFileHelper = MekanismDataGenerator.createFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        pack.addProvider((output, registries) -> new BasePackMetadataGenerator(output, DefenseLang.PACK_DESCRIPTION));
        //Client side data generators
        pack.addProvider((output, registries) -> new DefenseLangProvider(output));
        //Server side data generators
        pack.addProvider((output, registries) -> new DefenseTagProvider(output, registries));
        pack.addProvider((output, registries) -> new DefenseLootProvider(output));
        pack.addProvider((output, registries) -> new DefenseRecipeProvider(output, existingFileHelper));
    }
}