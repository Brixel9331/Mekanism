package mekanism.additions.client;

import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import mekanism.additions.common.MekanismAdditions;
import mekanism.client.texture.BaseSpriteSourceProvider;
import net.minecraft.data.PackOutput;

public class AdditionsSpriteSourceProvider extends BaseSpriteSourceProvider {

    public AdditionsSpriteSourceProvider(PackOutput output, ExistingFileHelper fileHelper) {
        super(output, MekanismAdditions.MODID, fileHelper);
    }

    @Override
    protected void addSources() {
        SourceList atlas = atlas(BLOCKS_ATLAS);
        addFiles(atlas, MekanismAdditions.rl("entity/balloon"), MekanismAdditions.rl("entity/balloon_string"));
    }
}