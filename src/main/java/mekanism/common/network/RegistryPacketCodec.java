package mekanism.common.network;

import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public final class RegistryPacketCodec {

    private RegistryPacketCodec() {
    }

    @SuppressWarnings("unchecked")
    public static <T> Registry<T> readRegistry(FriendlyByteBuf buffer) {
        ResourceLocation name = buffer.readResourceLocation();
        Registry<?> registry = BuiltInRegistries.REGISTRY.get(name);
        if (registry == null) {
            throw new DecoderException("Unknown registry: " + name);
        }
        return (Registry<T>) registry;
    }

    public static <T> T readValue(FriendlyByteBuf buffer, Registry<T> registry) {
        int id = buffer.readVarInt();
        return registry.getHolder(id).orElseThrow(() -> new DecoderException("Unknown entry " + id + " in " + registry.key().location())).value();
    }

    public static <T> T read(FriendlyByteBuf buffer, Registry<T> expected) {
        Registry<T> registry = readRegistry(buffer);
        if (registry != expected) {
            throw new DecoderException("Expected registry " + expected.key().location() + ", received " + registry.key().location());
        }
        return readValue(buffer, registry);
    }

    public static <T> void writeValue(FriendlyByteBuf buffer, Registry<T> registry, T value) {
        int id = registry.getId(value);
        if (id < 0 || registry.getResourceKey(value).isEmpty()) {
            throw new EncoderException("Unregistered entry in " + registry.key().location());
        }
        buffer.writeVarInt(id);
    }

    public static <T> void write(FriendlyByteBuf buffer, Registry<T> registry, T value) {
        buffer.writeResourceLocation(registry.key().location());
        writeValue(buffer, registry, value);
    }
}
