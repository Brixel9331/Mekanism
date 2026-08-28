package mekanism.common.world.modifier;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.biome.v1.BiomeModificationContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;

public interface BiomeModifier {

    void modify(BiomeSelectionContext biome, ModificationPhase phase, BiomeModificationContext context);

    Codec<? extends BiomeModifier> codec();
}
