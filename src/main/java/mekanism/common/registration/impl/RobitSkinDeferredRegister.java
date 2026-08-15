package mekanism.common.registration.impl;

import com.mojang.serialization.Codec;
import java.util.function.Function;
import java.util.function.Supplier;
import mekanism.api.MekanismAPI;
import mekanism.api.robit.RobitSkin;
import mekanism.common.registration.DeferredRegister;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class RobitSkinDeferredRegister extends DeferredRegister<Codec<? extends RobitSkin>> {

    private final String modid;
    private Codec<RobitSkin> directCodec;

    public RobitSkinDeferredRegister(String modid) {
        super(modid, MekanismAPI.ROBIT_SKIN_SERIALIZER_REGISTRY_NAME, MekanismAPI::robitSkinSerializerRegistry);
        this.modid = modid;
    }

    public Codec<RobitSkin> createAndRegisterDatapack(Codec<RobitSkin> networkCodec) {
        if (directCodec == null) {
            register();
            Codec<RobitSkin> codec = MekanismAPI.robitSkinSerializerRegistry().byNameCodec().dispatch(RobitSkin::codec, Function.identity());
            DynamicRegistries.registerSynced(MekanismAPI.ROBIT_SKIN_REGISTRY_NAME, codec, networkCodec);
            directCodec = codec;
        }
        return directCodec;
    }

    public ResourceKey<RobitSkin> dataKey(String name) {
        return ResourceKey.create(MekanismAPI.ROBIT_SKIN_REGISTRY_NAME, new ResourceLocation(modid, name));
    }

    public <ROBIT_SKIN extends RobitSkin> RobitSkinSerializerRegistryObject<ROBIT_SKIN> registerSerializer(String name, Supplier<Codec<ROBIT_SKIN>> sup) {
        return register(name, sup, RobitSkinSerializerRegistryObject::new);
    }
}