package mekanism.additions.client;

import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.registries.AdditionsSounds;
import mekanism.client.sound.BaseSoundProvider;
import net.minecraft.data.PackOutput;

public class AdditionsSoundProvider extends BaseSoundProvider {

    public AdditionsSoundProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, existingFileHelper, MekanismAdditions.MODID);
    }

    @Override
    public void registerSounds() {
        addSoundEventWithSubtitle(AdditionsSounds.POP, "pop");
    }
}