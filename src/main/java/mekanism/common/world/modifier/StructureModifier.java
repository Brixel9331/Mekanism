package mekanism.common.world.modifier;

import com.mojang.serialization.Codec;
import java.util.Map;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;

public interface StructureModifier {

    void modify(Holder<Structure> structure, ModificationPhase phase, Map<MobCategory, StructureSpawnOverride> spawns);

    Codec<? extends StructureModifier> codec();
}
