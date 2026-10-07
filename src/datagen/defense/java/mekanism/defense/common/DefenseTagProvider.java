package mekanism.defense.common;

import java.util.concurrent.CompletableFuture;
import mekanism.common.tag.BaseTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

public class DefenseTagProvider extends BaseTagProvider {

    public DefenseTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, MekanismDefense.MODID);
    }

    @Override
    protected void registerTags(HolderLookup.Provider registries) {
    }
}