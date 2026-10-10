package mekanism.additions.client;

import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import mekanism.additions.common.MekanismAdditions;
import mekanism.client.model.BaseBlockModelProvider;
import net.minecraft.data.PackOutput;

public class AdditionsBlockModelProvider extends BaseBlockModelProvider {

    //TODO: Add helpers for the color block stuff
    public AdditionsBlockModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MekanismAdditions.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {

    }
}