package mekanism.common.registration.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import mekanism.common.registration.DeferredRegister;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;

public class EntityTypeDeferredRegister extends DeferredRegister<EntityType<?>> {

    private Map<EntityTypeRegistryObject<? extends LivingEntity>, Supplier<Builder>> livingEntityAttributes = new HashMap<>();

    public EntityTypeDeferredRegister(String modid) {
        super(modid, Registries.ENTITY_TYPE, () -> BuiltInRegistries.ENTITY_TYPE);
    }

    public <ENTITY extends LivingEntity> EntityTypeRegistryObject<ENTITY> register(String name, FabricEntityTypeBuilder<ENTITY> builder, Supplier<Builder> attributes) {
        EntityTypeRegistryObject<ENTITY> entityTypeRO = register(name, builder);
        livingEntityAttributes.put(entityTypeRO, attributes);
        return entityTypeRO;
    }

    public <ENTITY extends Entity> EntityTypeRegistryObject<ENTITY> register(String name, FabricEntityTypeBuilder<ENTITY> builder) {
        return register(name, builder::build, EntityTypeRegistryObject::new);
    }

    @Override
    public void register() {
        super.register();
        if (livingEntityAttributes != null) {
            //Register our living entity attributes
            for (Map.Entry<EntityTypeRegistryObject<? extends LivingEntity>, Supplier<Builder>> entry : livingEntityAttributes.entrySet()) {
                FabricDefaultAttributeRegistry.register(entry.getKey().get(), entry.getValue().get().build());
            }
            //And set the map to null to allow it to be garbage collected
            livingEntityAttributes = null;
        }
    }
}
