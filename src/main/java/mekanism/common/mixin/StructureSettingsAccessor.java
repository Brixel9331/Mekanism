package mekanism.common.mixin;

import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Structure.class)
public interface StructureSettingsAccessor {

    @Mutable
    @Accessor("settings")
    void mekanism$setSettings(Structure.StructureSettings settings);
}
