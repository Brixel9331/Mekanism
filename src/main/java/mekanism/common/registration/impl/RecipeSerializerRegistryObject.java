package mekanism.common.registration.impl;

import mekanism.common.registration.RegistryObject;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class RecipeSerializerRegistryObject<RECIPE extends Recipe<?>> extends RegistryObject<RecipeSerializer<RECIPE>> {

    public RecipeSerializerRegistryObject(RegistryObject<RecipeSerializer<RECIPE>> registryObject) {
        super(registryObject);
    }
}