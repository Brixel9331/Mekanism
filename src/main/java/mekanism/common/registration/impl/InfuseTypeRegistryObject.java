package mekanism.common.registration.impl;

import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.providers.IInfuseTypeProvider;
import mekanism.common.registration.RegistryObject;
import org.jetbrains.annotations.NotNull;

public class InfuseTypeRegistryObject<INFUSE_TYPE extends InfuseType> extends RegistryObject<INFUSE_TYPE> implements IInfuseTypeProvider {

    public InfuseTypeRegistryObject(RegistryObject<INFUSE_TYPE> registryObject) {
        super(registryObject);
    }

    @NotNull
    @Override
    public INFUSE_TYPE getChemical() {
        return get();
    }
}