package mekanism.common.registration.impl;

import java.util.Objects;
import java.util.function.Supplier;
import mekanism.common.registration.INamedEntry;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.resources.ResourceLocation;

public final class DataSerializerRegistryObject<T> implements Supplier<EntityDataSerializer<T>>, INamedEntry {

    private final ResourceLocation id;
    private final Supplier<EntityDataSerializer<T>> supplier;
    private EntityDataSerializer<T> serializer;

    DataSerializerRegistryObject(ResourceLocation id, Supplier<EntityDataSerializer<T>> supplier) {
        this.id = id;
        this.supplier = Objects.requireNonNull(supplier);
    }

    void register() {
        if (serializer == null) {
            EntityDataSerializer<T> value = Objects.requireNonNull(supplier.get(), "Entity data serializer supplier returned null");
            if (EntityDataSerializers.getSerializedId(value) >= 0) {
                throw new IllegalArgumentException("Entity data serializer is already registered: " + id);
            }
            EntityDataSerializers.registerSerializer(value);
            serializer = value;
        }
    }

    @Override
    public EntityDataSerializer<T> get() {
        if (serializer == null) {
            throw new IllegalStateException("Entity data serializer has not been registered: " + id);
        }
        return serializer;
    }

    public ResourceLocation getId() {
        return id;
    }

    @Override
    public String getInternalRegistryName() {
        return id.getPath();
    }
}
