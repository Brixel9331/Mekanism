package mekanism.common.registration.impl;

import java.util.function.Supplier;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfuseTypeBuilder;
import mekanism.common.registration.DeferredRegister;
import net.minecraft.resources.ResourceLocation;

public class InfuseTypeDeferredRegister extends DeferredRegister<InfuseType> {

    public InfuseTypeDeferredRegister(String modid) {
        super(modid, MekanismAPI.INFUSE_TYPE_REGISTRY_NAME, MekanismAPI::infuseTypeRegistry);
    }

    public InfuseTypeRegistryObject<InfuseType> register(String name, int tint) {
        return register(name, () -> new InfuseType(InfuseTypeBuilder.builder().tint(tint)));
    }

    public InfuseTypeRegistryObject<InfuseType> register(String name, ResourceLocation texture, int barColor) {
        return register(name, () -> new InfuseType(InfuseTypeBuilder.builder(texture)) {
            @Override
            public int getColorRepresentation() {
                return barColor;
            }
        });
    }

    public <INFUSE_TYPE extends InfuseType> InfuseTypeRegistryObject<INFUSE_TYPE> register(String name, Supplier<? extends INFUSE_TYPE> sup) {
        return register(name, sup, InfuseTypeRegistryObject::new);
    }
}