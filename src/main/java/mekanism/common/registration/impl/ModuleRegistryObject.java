package mekanism.common.registration.impl;

import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.ModuleData;
import mekanism.api.providers.IModuleDataProvider;
import mekanism.common.registration.RegistryObject;
import org.jetbrains.annotations.NotNull;

public class ModuleRegistryObject<MODULE extends ICustomModule<MODULE>> extends RegistryObject<ModuleData<MODULE>> implements IModuleDataProvider<MODULE> {

    public ModuleRegistryObject(RegistryObject<ModuleData<MODULE>> registryObject) {
        super(registryObject);
    }

    @NotNull
    @Override
    public ModuleData<MODULE> getModuleData() {
        return get();
    }
}
