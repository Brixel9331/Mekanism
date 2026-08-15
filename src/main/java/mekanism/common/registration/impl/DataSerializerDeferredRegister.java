package mekanism.common.registration.impl;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class DataSerializerDeferredRegister {

    private final String modid;
    private final Map<ResourceLocation, DataSerializerRegistryObject<?>> entries = new LinkedHashMap<>();
    private boolean started;
    private boolean registered;

    public DataSerializerDeferredRegister(String modid) {
        this.modid = Objects.requireNonNull(modid);
    }

    public <T extends Enum<T>> DataSerializerRegistryObject<T> registerEnum(String name, Class<T> enumClass) {
        return register(name, () -> EntityDataSerializer.simpleEnum(enumClass));
    }

    public <T> DataSerializerRegistryObject<T> registerSimple(String name, FriendlyByteBuf.Writer<T> writer, FriendlyByteBuf.Reader<T> reader) {
        return register(name, () -> EntityDataSerializer.simple(writer, reader));
    }

    public <T> DataSerializerRegistryObject<T> register(String name, FriendlyByteBuf.Writer<T> writer, FriendlyByteBuf.Reader<T> reader, UnaryOperator<T> copier) {
        return register(name, () -> new EntityDataSerializer<>() {
            @Override
            public void write(@NotNull FriendlyByteBuf buffer, @NotNull T value) {
                writer.accept(buffer, value);
            }

            @NotNull
            @Override
            public T read(@NotNull FriendlyByteBuf buffer) {
                return reader.apply(buffer);
            }

            @NotNull
            @Override
            public T copy(@NotNull T value) {
                return copier.apply(value);
            }
        });
    }

    public <T> DataSerializerRegistryObject<T> register(String name, Supplier<EntityDataSerializer<T>> sup) {
        if (started) {
            throw new IllegalStateException("Entity data serializer registration has already started");
        }
        ResourceLocation id = new ResourceLocation(modid, name);
        if (entries.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate entity data serializer: " + id);
        }
        DataSerializerRegistryObject<T> entry = new DataSerializerRegistryObject<>(id, sup);
        entries.put(id, entry);
        return entry;
    }

    public void register() {
        if (registered) {
            return;
        } else if (started) {
            throw new IllegalStateException("Entity data serializer registration did not complete");
        }
        started = true;
        for (DataSerializerRegistryObject<?> entry : entries.values()) {
            entry.register();
        }
        registered = true;
    }
}
