package mekanism.common.registration.impl;

import mekanism.api.providers.IItemProvider;
import mekanism.common.registration.RegistryObject;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

public class ItemRegistryObject<ITEM extends Item> extends RegistryObject<ITEM> implements IItemProvider {

    public ItemRegistryObject(RegistryObject<ITEM> registryObject) {
        super(registryObject);
    }

    @NotNull
    @Override
    public ITEM asItem() {
        return get();
    }
}