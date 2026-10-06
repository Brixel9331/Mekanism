package mekanism.common.registration.impl;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import mekanism.common.registration.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class PlacementModifierDeferredRegister extends DeferredRegister<PlacementModifierType<?>> {

    public PlacementModifierDeferredRegister(String modid) {
        super(modid, Registries.PLACEMENT_MODIFIER_TYPE, () -> BuiltInRegistries.PLACEMENT_MODIFIER_TYPE);
    }

    public <PROVIDER extends PlacementModifier> PlacementModifierRegistryObject<PROVIDER> register(String name, Codec<PROVIDER> codec) {
        return register(name, () -> () -> codec);
    }

    public <PROVIDER extends PlacementModifier> PlacementModifierRegistryObject<PROVIDER> register(String name, Supplier<? extends PlacementModifierType<PROVIDER>> sup) {
        return register(name, sup, PlacementModifierRegistryObject::new);
    }
}