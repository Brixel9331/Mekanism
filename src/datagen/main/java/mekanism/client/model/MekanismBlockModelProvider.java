package mekanism.client.model;

import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import mekanism.common.Mekanism;
import net.minecraft.data.PackOutput;

public class MekanismBlockModelProvider extends BaseBlockModelProvider {

    public MekanismBlockModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Mekanism.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {

    }
}