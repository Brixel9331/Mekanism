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

class DeferredRegisterTest {

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void declarationsKeepNamesAvailableWithoutEvaluatingSuppliers() {
        Registry<String> registry = registry("deferred");
        AtomicInteger evaluations = new AtomicInteger();
        DeferredRegister<String> entries = new DeferredRegister<>("test", registry.key(), () -> registry);
        RegistryObject<String> entry = entries.registerEntry("first", () -> {
            evaluations.incrementAndGet();
            return "first";
        });
        RegistryObject<String> wrapped = new RegistryObject<>(entry);
        assertEquals(new ResourceLocation("test", "first"), entry.getId());
        assertEquals("first", entry.getInternalRegistryName());
        assertEquals(ResourceKey.create(registry.key(), entry.getId()), entry.key());
        assertEquals(0, evaluations.get());
        assertThrows(IllegalStateException.class, entry::get);
        assertThrows(IllegalStateException.class, wrapped::get);
        entries.register();
        assertEquals(1, evaluations.get());
        assertEquals("first", entry.get());
        assertSame(entry.get(), wrapped.get());
        assertSame(entry.get(), registry.get(entry.getId()));
        entries.register();
        assertEquals(1, evaluations.get());
    }

    @Test
    void registrationPreservesDeclarationOrderAndEarlierReferences() {
        Registry<String> registry = registry("ordered");
        DeferredRegister<String> entries = new DeferredRegister<>("test", registry.key(), () -> registry);
        RegistryObject<String> first = entries.registerEntry("first", () -> "first");
        RegistryObject<String> second = entries.registerEntry("second", () -> first.get() + ":second");
        entries.register();
        assertEquals("first:second", second.get());
        assertEquals(0, registry.getId(first.get()));
        assertEquals(1, registry.getId(second.get()));
        assertThrows(IllegalStateException.class, () -> entries.registerEntry("late", () -> "late"));
    }

    @Test
    void duplicateDeclarationsDoNotReplaceTheFirstSupplier() {
        Registry<String> registry = registry("duplicates");
        DeferredRegister<String> entries = new DeferredRegister<>("test", registry.key(), () -> registry);
        RegistryObject<String> first = entries.registerEntry("entry", () -> "first");
        assertThrows(IllegalArgumentException.class, () -> entries.registerEntry("entry", () -> "second"));
        entries.register();
        assertEquals("first", first.get());
    }

    @Test
    void registryCollisionsAreRejectedBeforeAnySupplierRuns() {
        Registry<String> registry = registry("collisions");
        Registry.register(registry, new ResourceLocation("test", "existing"), "original");
        AtomicInteger evaluations = new AtomicInteger();
        DeferredRegister<String> entries = new DeferredRegister<>("test", registry.key(), () -> registry);
        RegistryObject<String> first = entries.registerEntry("new", () -> {
            evaluations.incrementAndGet();
            return "new";
        });
        entries.registerEntry("existing", () -> "replacement");
        assertThrows(IllegalStateException.class, entries::register);
        assertEquals(0, evaluations.get());
        assertThrows(IllegalStateException.class, first::get);
        assertEquals("original", registry.get(new ResourceLocation("test", "existing")));
        assertThrows(IllegalStateException.class, entries::register);
    }

    @Test
    void supplierFailuresCannotBeRetriedAsSuccessfulRegistration() {
        Registry<String> registry = registry("failure");
        DeferredRegister<String> entries = new DeferredRegister<>("test", registry.key(), () -> registry);
        RegistryObject<String> first = entries.registerEntry("first", () -> "first");
        RegistryObject<String> broken = entries.registerEntry("broken", () -> {
            throw new IllegalArgumentException("broken supplier");
        });
        assertThrows(IllegalArgumentException.class, entries::register);
        assertEquals("first", first.get());
        assertThrows(IllegalStateException.class, broken::get);
        assertThrows(IllegalStateException.class, entries::register);
        assertThrows(IllegalStateException.class, () -> entries.registerEntry("later", () -> "later"));
    }

    @Test
    void mismatchedRegistryKeysAreRejected() {
        Registry<String> registry = registry("actual");
        Registry<String> other = registry("wrong");
        DeferredRegister<String> entries = new DeferredRegister<>("test", other.key(), () -> registry);
        entries.registerEntry("entry", () -> "value");
        assertThrows(IllegalArgumentException.class, entries::register);
        assertEquals(0, registry.size());
    }

    @Test
    void subtypeEntriesKeepTheirDeclaredType() {
        ResourceKey<Registry<Number>> key = ResourceKey.createRegistryKey(new ResourceLocation("test", "numbers"));
        Registry<Number> registry = new MappedRegistry<>(key, Lifecycle.stable(), false);
        DeferredRegister<Number> entries = new DeferredRegister<>("test", key, () -> registry);
        RegistryObject<Integer> integer = entries.registerEntry("integer", () -> 123);
        RegistryObject<Double> decimal = entries.registerEntry("decimal", () -> 1.5);
        entries.register();
        assertEquals(123, integer.get());
        assertEquals(1.5, decimal.get());
        assertSame(integer.get(), registry.get(integer.getId()));
    }

    private static Registry<String> registry(String name) {
        return new MappedRegistry<>(ResourceKey.createRegistryKey(new ResourceLocation("test", name)), Lifecycle.stable(), false);
    }
}
