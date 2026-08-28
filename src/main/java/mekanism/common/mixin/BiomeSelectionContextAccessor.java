package mekanism.common.mixin;

import net.fabricmc.fabric.impl.biome.modification.BiomeSelectionContextImpl;
import net.minecraft.core.RegistryAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = BiomeSelectionContextImpl.class, remap = false)
public interface BiomeSelectionContextAccessor {

    @Accessor("dynamicRegistries")
    RegistryAccess mekanism$getRegistries();
}
