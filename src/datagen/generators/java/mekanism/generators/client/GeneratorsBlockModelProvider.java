package mekanism.generators.client;

import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import mekanism.client.model.BaseBlockModelProvider;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.data.PackOutput;

public class GeneratorsBlockModelProvider extends BaseBlockModelProvider {

    public GeneratorsBlockModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MekanismGenerators.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {

    }
}