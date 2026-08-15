package mekanism.common.registration.impl;

import mekanism.common.registration.RegistryObject;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class PlacementModifierRegistryObject<PROVIDER extends PlacementModifier> extends RegistryObject<PlacementModifierType<PROVIDER>> {

    public PlacementModifierRegistryObject(RegistryObject<PlacementModifierType<PROVIDER>> registryObject) {
        super(registryObject);
    }
}