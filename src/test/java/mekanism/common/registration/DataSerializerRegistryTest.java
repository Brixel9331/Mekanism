package mekanism.common.registration;

import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import mekanism.api.security.SecurityMode;
import mekanism.common.registration.impl.DataSerializerDeferredRegister;
import mekanism.common.registration.impl.DataSerializerRegistryObject;
import net.minecraft.SharedConstants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DataSerializerRegistryTest {

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void serializersRetainDeclarationOrderAndRoundTripData() {
        DataSerializerDeferredRegister serializers = new DataSerializerDeferredRegister("test");
        DataSerializerRegistryObject<SecurityMode> mode = serializers.registerEnum("mode", SecurityMode.class);
        DataSerializerRegistryObject<UUID> owner = serializers.registerSimple("owner", FriendlyByteBuf::writeUUID, FriendlyByteBuf::readUUID);
        assertThrows(IllegalStateException.class, mode::get);
        serializers.register();
        int modeId = EntityDataSerializers.getSerializedId(mode.get());
        int ownerId = EntityDataSerializers.getSerializedId(owner.get());
        assertEquals(modeId + 1, ownerId);
        assertSame(mode.get(), EntityDataSerializers.getSerializer(modeId));
        assertSame(owner.get(), EntityDataSerializers.getSerializer(ownerId));
        serializers.register();
        assertEquals(ownerId, EntityDataSerializers.getSerializedId(owner.get()));
        UUID value = UUID.randomUUID();
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            mode.get().write(buffer, SecurityMode.TRUSTED);
            owner.get().write(buffer, value);
            assertEquals(SecurityMode.TRUSTED, mode.get().read(buffer));
            assertEquals(value, owner.get().read(buffer));
            assertEquals(0, buffer.readableBytes());
        } finally {
            buffer.release();
        }
    }

    @Test
    void customCopyFunctionsKeepMutableTrackedDataIndependent() {
        DataSerializerDeferredRegister serializers = new DataSerializerDeferredRegister("test");
        DataSerializerRegistryObject<ArrayList<Integer>> values = serializers.register("values",
              (buffer, value) -> buffer.writeCollection(value, FriendlyByteBuf::writeVarInt),
              buffer -> buffer.readCollection(ArrayList::new, FriendlyByteBuf::readVarInt), ArrayList::new);
        serializers.register();
        ArrayList<Integer> original = new ArrayList<>();
        original.add(1);
        ArrayList<Integer> copy = values.get().copy(original);
        copy.add(2);
        assertEquals(1, original.size());
        assertEquals(2, copy.size());
    }

    @Test
    void duplicateAndLateDeclarationsCannotReplaceSerializers() {
        DataSerializerDeferredRegister serializers = new DataSerializerDeferredRegister("test");
        AtomicInteger calls = new AtomicInteger();
        DataSerializerRegistryObject<UUID> original = serializers.registerSimple("owner", FriendlyByteBuf::writeUUID, FriendlyByteBuf::readUUID);
        assertThrows(IllegalArgumentException.class, () -> serializers.register("owner", () -> {
            calls.incrementAndGet();
            return EntityDataSerializers.INT;
        }));
        serializers.register();
        assertEquals(0, calls.get());
        assertNotNull(original.get());
        assertThrows(IllegalStateException.class, () -> serializers.registerEnum("late", SecurityMode.class));
    }

    @Test
    void failedRegistrationCannotBeRetriedWithDifferentNetworkIds() {
        DataSerializerDeferredRegister serializers = new DataSerializerDeferredRegister("test");
        serializers.register("existing", () -> EntityDataSerializers.INT);
        assertThrows(IllegalArgumentException.class, serializers::register);
        assertThrows(IllegalStateException.class, serializers::register);
    }
}
