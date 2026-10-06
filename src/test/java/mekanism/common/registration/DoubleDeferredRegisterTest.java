package mekanism.common.registration;

import com.mojang.serialization.Lifecycle;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.SharedConstants;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DoubleDeferredRegisterTest {

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void secondaryFactoriesCanResolveAllPrimaryEntries() {
        Registry<String> primary = registry("blocks");
        Registry<String> secondary = registry("items");
        DoubleDeferredRegister<String, String> entries = new DoubleDeferredRegister<>("test", primary, secondary);
        AtomicInteger calls = new AtomicInteger();
        DoubleWrappedRegistryObject<String, String> first = entries.register("first", () -> "first block", block -> {
            calls.incrementAndGet();
            return block + " item";
        }, DoubleWrappedRegistryObject::new);
        DoubleWrappedRegistryObject<String, String> second = entries.registerAdvanced("second", () -> "second block",
              block -> block.getId() + ":" + first.getPrimary(), DoubleWrappedRegistryObject::new);
        assertThrows(IllegalStateException.class, first::getPrimary);
        assertThrows(IllegalStateException.class, first::getSecondary);
        assertEquals("first", first.getInternalRegistryName());
        entries.register();
        assertSame(first.getPrimary(), primary.get(new ResourceLocation("test", "first")));
        assertSame(first.getSecondary(), secondary.get(new ResourceLocation("test", "first")));
        assertEquals("first block item", first.getSecondary());
        assertEquals("test:second:first block", second.getSecondary());
        entries.register();
        assertEquals(1, calls.get());
    }

    @Test
    void primaryRegistrationFailuresNeverStartSecondaryFactories() {
        Registry<String> primary = registry("failed_blocks");
        Registry<String> secondary = registry("unused_items");
        DoubleDeferredRegister<String, String> entries = new DoubleDeferredRegister<>("test", primary, secondary);
        AtomicInteger calls = new AtomicInteger();
        entries.register("broken", () -> {
            throw new IllegalStateException("broken block");
        }, () -> {
            calls.incrementAndGet();
            return "item";
        }, DoubleWrappedRegistryObject::new);
        assertThrows(IllegalStateException.class, entries::register);
        assertThrows(IllegalStateException.class, entries::register);
        assertEquals(0, calls.get());
        assertEquals(0, secondary.size());
    }

    private static Registry<String> registry(String path) {
        return new MappedRegistry<>(ResourceKey.createRegistryKey(new ResourceLocation("test", path)), Lifecycle.stable(), false);
    }
}
