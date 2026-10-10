package mekanism.generators.client;

import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import mekanism.client.state.BaseBlockStateProvider;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.registries.GeneratorsFluids;
import net.minecraft.data.PackOutput;

public class GeneratorsBlockStateProvider extends BaseBlockStateProvider<GeneratorsBlockModelProvider> {

    public GeneratorsBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MekanismGenerators.MODID, existingFileHelper, GeneratorsBlockModelProvider::new);
    }

    @Override
    protected void registerStatesAndModels() {
        registerFluidBlockStates(GeneratorsFluids.FLUIDS.getAllFluids());
    }
}